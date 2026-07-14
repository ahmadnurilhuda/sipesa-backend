create table users (
    id uuid primary key,
    name varchar(160) not null,
    username varchar(80) not null unique,
    email varchar(160) not null unique,
    phone varchar(40),
    password_hash varchar(255) not null,
    role varchar(40) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table academic_years (
    id uuid primary key,
    period varchar(30) not null unique,
    active boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table school_classes (
    id uuid primary key,
    name varchar(80) not null,
    school_year varchar(20),
    academic_year_id uuid references academic_years(id),
    homeroom_teacher_id uuid references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table rooms (
    id uuid primary key,
    name varchar(80) not null,
    building varchar(80),
    guardian_id uuid references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table parent_guardians (
    id uuid primary key,
    name varchar(160) not null,
    phone varchar(40),
    address text,
    user_id uuid references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table students (
    id uuid primary key,
    nis varchar(60) unique,
    name varchar(160) not null,
    gender varchar(20),
    active boolean not null default true,
    class_id uuid references school_classes(id),
    room_id uuid references rooms(id),
    parent_guardian_id uuid references parent_guardians(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_students_gender check (gender is null or gender in ('L', 'P'))
);

create table pengurus (
    id uuid primary key,
    name varchar(160) not null,
    nip varchar(60),
    phone varchar(40) unique,
    position varchar(100) not null,
    user_id uuid references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table class_guardians (
    id uuid primary key,
    pengurus_id uuid not null references pengurus(id),
    class_id uuid not null references school_classes(id),
    academic_year_id uuid not null references academic_years(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (pengurus_id, class_id, academic_year_id)
);

create table room_guardians (
    id uuid primary key,
    pengurus_id uuid not null references pengurus(id),
    room_id uuid not null references rooms(id),
    academic_year_id uuid not null references academic_years(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (pengurus_id, room_id, academic_year_id)
);

create table student_class_histories (
    id uuid primary key,
    student_id uuid not null references students(id),
    class_id uuid not null references school_classes(id),
    academic_year_id uuid not null references academic_years(id),
    start_date date,
    end_date date,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (student_id, class_id, academic_year_id)
);

create table student_room_histories (
    id uuid primary key,
    student_id uuid not null references students(id),
    room_id uuid not null references rooms(id),
    academic_year_id uuid references academic_years(id),
    start_date date,
    end_date date,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (student_id, room_id, academic_year_id)
);

create table semesters (
    id uuid primary key,
    academic_year_id uuid not null references academic_years(id),
    name varchar(20) not null,
    start_date date not null,
    end_date date not null,
    max_permission_days integer not null default 9,
    active boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint semesters_name_check check (name in ('GANJIL', 'GENAP')),
    constraint semesters_date_check check (end_date >= start_date),
    constraint semesters_max_permission_days_check check (max_permission_days > 0),
    unique (academic_year_id, name)
);

create table permission_requests (
    id uuid primary key,
    student_id uuid not null references students(id),
    requested_by_id uuid not null references users(id),
    permission_type varchar(80) not null,
    reason text not null,
    destination varchar(180) not null,
    start_at timestamptz not null,
    expected_return_at timestamptz not null,
    semester_id uuid references semesters(id),
    checked_out_at timestamptz,
    checked_in_at timestamptz,
    completed_at timestamptz,
    return_reminder_sent_at timestamptz,
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table permission_approval_logs (
    id uuid primary key,
    permission_request_id uuid not null references permission_requests(id),
    actor_id uuid not null references users(id),
    from_status varchar(40) not null,
    to_status varchar(40) not null,
    note text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table permission_qr_tokens (
    id uuid primary key,
    permission_request_id uuid not null unique references permission_requests(id),
    token varchar(96) not null unique,
    expires_at timestamptz not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table notifications (
    id uuid primary key,
    user_id uuid not null references users(id),
    title varchar(160) not null,
    message text not null,
    read boolean not null default false,
    channel varchar(30) not null default 'EMAIL',
    delivery_status varchar(30) not null default 'PENDING',
    delivery_error text,
    sent_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table password_reset_otps (
    id uuid primary key,
    user_id uuid not null references users(id),
    email varchar(160) not null,
    code_hash varchar(255) not null,
    reset_token_hash varchar(255),
    expires_at timestamptz not null,
    verified_at timestamptz,
    used_at timestamptz,
    attempt_count integer not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_users_phone on users(phone);
create unique index ux_pengurus_user_id on pengurus(user_id) where user_id is not null;
create index idx_permissions_status on permission_requests(status);
create index idx_permissions_student on permission_requests(student_id);
create index idx_permission_requests_student_semester_status on permission_requests(student_id, semester_id, status);
create index idx_permission_requests_return_reminder on permission_requests(status, expected_return_at, return_reminder_sent_at);
create index idx_notifications_user_read on notifications(user_id, read);
create index idx_notifications_delivery_status on notifications(delivery_status);
create index idx_password_reset_otps_email on password_reset_otps(email, used_at, created_at);
create index idx_student_class_histories_active on student_class_histories(student_id, active);
create index idx_student_room_histories_active on student_room_histories(student_id, active);
create index idx_semesters_date_range on semesters(start_date, end_date);
create index idx_school_classes_academic_year on school_classes(academic_year_id);

insert into users (id, name, username, email, phone, password_hash, role, active, created_at, updated_at) values
('00000000-0000-0000-0000-000000000001','Administrator','admin','admin@sipesa.local','081234567895','$2a$10$2obaRj4sUlzNkid/HWSEseqcnAoU3p7WeIGPGN4JwaTKnwWESfdDm','ADMIN',true,now(),now());
