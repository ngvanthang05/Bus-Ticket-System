#!/bin/bash
# ============================================================
# BUS TICKET SYSTEM - STARTUP SCRIPT
# ============================================================
# Cách dùng:
#   ./scripts/start.sh          - Khởi động toàn bộ infrastructure
#   ./scripts/start.sh -d       - Chạy background (detach mode)
#   ./scripts/start.sh --down   - Dừng toàn bộ
#   ./scripts/start.sh --clean  - Xóa hết container + volume
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_DIR/.env"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

print_banner() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "   BUS TICKET SYSTEM - Infrastructure Manager"
    echo "=================================================="
    echo -e "${NC}"
}

check_requirements() {
    log_info "Checking requirements..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed!"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed!"
        exit 1
    fi

    log_success "Docker: $(docker --version)"
    log_success "Requirements OK"
}

start_infrastructure() {
    local detach_flag=""
    if [ "$1" == "-d" ]; then
        detach_flag="-d"
    fi

    log_info "Starting infrastructure..."

    if [ ! -f "$ENV_FILE" ]; then
        log_warn ".env file not found. Using default passwords."
    fi

    # Start với thứ tự: databases trước, kafka/redis sau
    log_info "Step 1/3: Starting databases..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up \
        postgres-auth postgres-route postgres-vehicle \
        postgres-trip postgres-booking postgres-payment \
        postgres-notification \
        $detach_flag

    log_info "Step 2/3: Starting cache and message broker..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up \
        redis zookeeper kafka \
        $detach_flag

    log_info "Step 3/3: Starting monitoring UIs and init jobs..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up \
        kafka-ui redis-insight kafka-init \
        $detach_flag

    log_success "Infrastructure started!"
    print_endpoints
}

start_all() {
    log_info "Starting ALL services..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    log_success "All services started!"
    print_endpoints
}

stop_infrastructure() {
    log_info "Stopping infrastructure..."
    docker compose -f "$COMPOSE_FILE" down
    log_success "Infrastructure stopped."
}

clean_infrastructure() {
    log_warn "This will DELETE all containers AND volumes (all data will be lost)!"
    read -p "Are you sure? (yes/no): " confirm
    if [ "$confirm" == "yes" ]; then
        docker compose -f "$COMPOSE_FILE" down -v --remove-orphans
        log_success "All containers and volumes removed."
    else
        log_info "Cancelled."
    fi
}

check_health() {
    log_info "Checking service health..."
    docker compose -f "$COMPOSE_FILE" ps
}

print_endpoints() {
    echo ""
    echo -e "${GREEN}=================================================="
    echo "   SERVICE ENDPOINTS"
    echo "=================================================="
    echo -e "${NC}"
    echo "📊 Kafka UI:        http://localhost:8090"
    echo "🔴 Redis Insight:   http://localhost:5540"
    echo ""
    echo "🐘 PostgreSQL Databases:"
    echo "   Auth DB:         localhost:5433  (auth_db)"
    echo "   Route DB:        localhost:5434  (route_db)"
    echo "   Vehicle DB:      localhost:5435  (vehicle_db)"
    echo "   Trip DB:         localhost:5436  (trip_db)"
    echo "   Booking DB:      localhost:5437  (booking_db)"
    echo "   Payment DB:      localhost:5438  (payment_db)"
    echo "   Notification DB: localhost:5439  (notification_db)"
    echo ""
    echo "🚀 Redis:           localhost:6379"
    echo "📨 Kafka:           localhost:9092"
    echo ""
}

# ---- Main ----
print_banner
check_requirements

case "${1:-}" in
    "--down")   stop_infrastructure ;;
    "--clean")  clean_infrastructure ;;
    "--health") check_health ;;
    "--all")    start_all ;;
    "--help")
        echo "Usage: $0 [option]"
        echo "  (no option) - Start with logs"
        echo "  -d          - Start in background"
        echo "  --all       - Start all services in background"
        echo "  --down      - Stop all services"
        echo "  --clean     - Remove all containers and volumes"
        echo "  --health    - Check service health"
        ;;
    *)          start_infrastructure "$1" ;;
esac