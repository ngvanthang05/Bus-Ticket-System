#!/bin/bash
# ============================================================
# KAFKA TOPICS SETUP SCRIPT
# Run after Kafka is up: ./infrastructure/kafka/init-topics.sh
# ============================================================

KAFKA_CONTAINER="bus-kafka"
BOOTSTRAP_SERVER="localhost:9092"

echo "==> Waiting for Kafka to be ready..."
sleep 10

echo "==> Creating Kafka topics..."

# Booking events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic booking.created \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic booking.cancelled \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic booking.confirmed \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

# Payment events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic payment.initiated \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic payment.success \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic payment.failed \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

# Ticket events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic ticket.issued \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic ticket.checkin \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

# Seat events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic seat.locked \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic seat.released \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

# Notification events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic notification.send \
  --partitions 3 --replication-factor 1 \
  --if-not-exists

# Trip events
docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic trip.departed \
  --partitions 1 --replication-factor 1 \
  --if-not-exists

docker exec $KAFKA_CONTAINER kafka-topics --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic trip.arrived \
  --partitions 1 --replication-factor 1 \
  --if-not-exists

echo "==> Listing all topics:"
docker exec $KAFKA_CONTAINER kafka-topics --list \
  --bootstrap-server $BOOTSTRAP_SERVER

echo "==> Kafka topics setup complete!"