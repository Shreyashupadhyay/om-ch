-- Enable pgcrypto for gen_random_uuid() if not available natively
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- CONVERSATIONS TABLE
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_one_id UUID REFERENCES users(id) ON DELETE SET NULL,
    user_two_id UUID REFERENCES users(id) ON DELETE SET NULL,
    user_one_is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    user_two_is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    user_one_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    user_two_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE,
    ended_by VARCHAR(50)
);

-- MESSAGES TABLE
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    sender_label VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- REPORTS TABLE
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reporter_guest_id VARCHAR(100),
    conversation_id UUID REFERENCES conversations(id) ON DELETE SET NULL,
    reason VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- BLOCKS TABLE
CREATE TABLE IF NOT EXISTS blocks (
    blocker_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_user_id, blocked_user_id)
);

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_conversations_user_one ON conversations(user_one_id);
CREATE INDEX IF NOT EXISTS idx_conversations_user_two ON conversations(user_two_id);
CREATE INDEX IF NOT EXISTS idx_conversations_started_at ON conversations(started_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_sent ON messages(conversation_id, sent_at ASC);
CREATE INDEX IF NOT EXISTS idx_reports_conversation ON reports(conversation_id);
CREATE INDEX IF NOT EXISTS idx_blocks_blocker ON blocks(blocker_user_id);
