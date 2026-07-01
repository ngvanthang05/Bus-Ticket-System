-- ============================================================
-- NOTIFICATION SERVICE DATABASE SCHEMA
-- Database: notification_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: notification_templates
-- ============================================================
CREATE TABLE IF NOT EXISTS notification_templates (
                                                      id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_code   VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    -- SMS, EMAIL, PUSH
    subject         VARCHAR(500),
    -- Subject cho email
    body_template   TEXT NOT NULL,
    -- Template với placeholders: {{customer_name}}, {{booking_code}}, etc.
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: notifications (Log thông báo đã gửi)
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
                                             id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id    UUID,
    recipient_phone VARCHAR(20),
    recipient_email VARCHAR(255),
    channel         VARCHAR(50) NOT NULL,
    template_code   VARCHAR(100),
    subject         VARCHAR(500),
    body            TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: Chờ gửi, SENT: Đã gửi, FAILED: Thất bại, DELIVERED: Đã nhận
    provider        VARCHAR(100),
    -- TWILIO, SENDGRID, FIREBASE, etc.
    provider_message_id VARCHAR(255),
    retry_count     INTEGER NOT NULL DEFAULT 0,
    error_message   TEXT,
    reference_id    UUID,
    -- booking_id, payment_id để truy xuất
    reference_type  VARCHAR(50),
    -- BOOKING, PAYMENT, TICKET
    sent_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_notifications_recipient  ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status     ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_reference  ON notifications(reference_id, reference_type);
CREATE INDEX IF NOT EXISTS idx_notifications_channel    ON notifications(channel);

-- ============================================================
-- SEED DATA: Default Templates
-- ============================================================
INSERT INTO notification_templates (template_code, name, channel, subject, body_template) VALUES
                                                                                              (
                                                                                                  'BOOKING_CONFIRMED_SMS',
                                                                                                  'Xác nhận đặt vé - SMS',
                                                                                                  'SMS',
                                                                                                  NULL,
                                                                                                  'Ve xe cua ban da duoc xac nhan. Ma dat ve: {{booking_code}}. Chuyen xe: {{route_name}} luc {{departure_time}}. So ghe: {{seat_numbers}}. Tong tien: {{total_amount}} VND.'
                                                                                              ),
                                                                                              (
                                                                                                  'BOOKING_CONFIRMED_EMAIL',
                                                                                                  'Xác nhận đặt vé - Email',
                                                                                                  'EMAIL',
                                                                                                  '[Xe Khách] Xác nhận đặt vé - {{booking_code}}',
                                                                                                  '<h2>Xác nhận đặt vé thành công!</h2>
                                                                                              <p>Kính chào <strong>{{customer_name}}</strong>,</p>
                                                                                              <p>Đơn đặt vé của bạn đã được xác nhận.</p>
                                                                                              <table>
                                                                                                <tr><td>Mã đặt vé:</td><td><strong>{{booking_code}}</strong></td></tr>
                                                                                                <tr><td>Tuyến:</td><td>{{route_name}}</td></tr>
                                                                                                <tr><td>Giờ khởi hành:</td><td>{{departure_time}}</td></tr>
                                                                                                <tr><td>Điểm đón:</td><td>{{pickup_stop}}</td></tr>
                                                                                                <tr><td>Số ghế:</td><td>{{seat_numbers}}</td></tr>
                                                                                                <tr><td>Tổng tiền:</td><td>{{total_amount}} VND</td></tr>
                                                                                              </table>
                                                                                              <p>Mã QR vé của bạn đính kèm bên dưới. Vui lòng xuất trình khi lên xe.</p>'
                                                                                              ),
                                                                                              (
                                                                                                  'PAYMENT_SUCCESS_SMS',
                                                                                                  'Thanh toán thành công - SMS',
                                                                                                  'SMS',
                                                                                                  NULL,
                                                                                                  'Thanh toan thanh cong {{amount}} VND cho dat ve {{booking_code}}. Ma giao dich: {{payment_code}}.'
                                                                                              ),
                                                                                              (
                                                                                                  'BOOKING_CANCELLED_SMS',
                                                                                                  'Hủy vé - SMS',
                                                                                                  'SMS',
                                                                                                  NULL,
                                                                                                  'Ve xe {{booking_code}} da duoc huy. So tien hoan lai: {{refund_amount}} VND se duoc xu ly trong 3-5 ngay lam viec.'
                                                                                              ),
                                                                                              (
                                                                                                  'TRIP_REMINDER_SMS',
                                                                                                  'Nhắc nhở trước chuyến - SMS',
                                                                                                  'SMS',
                                                                                                  NULL,
                                                                                                  'Nhac nho: Chuyen xe {{route_name}} cua ban se khoi hanh luc {{departure_time}} ngay mai. Diem don: {{pickup_stop}}. Ma ve: {{ticket_code}}.'
                                                                                              )
    ON CONFLICT (template_code) DO NOTHING;

-- Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_notifications_updated_at
    BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
