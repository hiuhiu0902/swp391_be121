-- ALTER TABLE quit_plans
-- ADD status varchar(255) NOT NULL DEFAULT 'ACTIVE'
-- CONSTRAINT check_quit_plan_status CHECK (status IN ('ACTIVE','COMPLETED','FAILED','DELETED'));

ALTER TABLE quit_plans
ADD status varchar(255) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE quit_plans
ADD CONSTRAINT check_quit_plan_status
CHECK (status IN ('ACTIVE','COMPLETED','FAILED','DELETED'));
