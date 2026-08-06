update users
set authority = 'WORKER'
where authority = 'BOX_CAT';

update users
set authority = 'MANAGER'
where authority = 'BOX_MANAGER';

-- 2. Удаляем старый констрейнт
alter table users
    drop constraint if exists chk_users_authority;

-- 3. Создаём новый констрейнт
alter table users
    add constraint chk_users_authority
        check (authority in ('WORKER', 'MANAGER', 'ADMIN'));