-- ============================================================
-- PAYMENT SERVICE DATABASE SCHEMA
-- Database: payment_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: payments (Thanh toán đơn đặt vé)
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
                                        id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_code        VARCHAR(50) NOT NULL UNIQUE,
    booking_id          UUID NOT NULL UNIQUE,
    customer_id         UUID NOT NULL,
    amount              DECIMAL(12, 2) NOT NULL,
    payment_method      VARCHAR(50) NOT NULL,
    -- CASH: Tiền mặt, QR_CODE: QR code, E_WALLET: Ví điện tử
    -- BANK_TRANSFER: Chuyển khoản, CREDIT_CARD: Thẻ tín dụng
    payment_provider    VARCHAR(100),
    -- MOMO, VNPAY, ZALOPAY, VIETCOMBANK, etc.
    provider_transaction_id VARCHAR(255),
    -- Transaction ID từ payment gateway
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: Chờ thanh toán, PROCESSING: Đang xử lý
    -- SUCCESS: Thành công, FAILED: Thất bại, REFUNDED: Đã hoàn
    paid_at             TIMESTAMP,
    expired_at          TIMESTAMP,
    -- Thời hạn thanh toán (mặc định 15 phút)
    metadata            JSONB,
    -- Thông tin thêm từ payment gateway
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: transactions (Lịch sử giao dịch)
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
                                            id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id      UUID NOT NULL REFERENCES payments(id),
    transaction_type VARCHAR(50) NOT NULL,
    -- CHARGE: Thu tiền, REFUND: Hoàn tiền
    amount          DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    provider_ref    VARCHAR(255),
    -- Reference từ payment provider
    request_data    JSONB,
    response_data   JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: refunds (Hoàn tiền)
-- ============================================================
CREATE TABLE IF NOT EXISTS refunds (
                                       id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    refund_code     VARCHAR(50) NOT NULL UNIQUE,
    payment_id      UUID NOT NULL REFERENCES payments(id),
    booking_id      UUID NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    refund_method   VARCHAR(50) NOT NULL,
    -- ORIGINAL: Hoàn về phương thức gốc, BANK: Chuyển khoản
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: Chờ xử lý, PROCESSING: Đang xử lý
    -- COMPLETED: Hoàn thành, FAILED: Thất bại
    reason          TEXT,
    bank_account    VARCHAR(50),
    bank_name       VARCHAR(100),
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_customer   ON payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_status     ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_code       ON payments(payment_code);
CREATE INDEX IF NOT EXISTS idx_transactions_payment ON transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_refunds_payment     ON refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refunds_booking     ON refunds(booking_id);
CREATE INDEX IF NOT EXISTS idx_refunds_status      ON refunds(status);

-- ============================================================
-- FUNCTION: Generate payment code
-- ============================================================
CREATE OR REPLACE FUNCTION generate_payment_code()
RETURNS VARCHAR AS $$
BEGIN
RETURN 'PAY' || TO_CHAR(NOW(), 'YYMMDD') || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 6));
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE OR REPLACE TRIGGER update_refunds_updated_at
    BEFORE UPDATE ON refunds FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();