create table if not exists asn (
                                   id                     uuid         not null,
                                   version                integer,
                                   asn_number             varchar(255) not null,
                                   external_id            varchar(255) not null,
                                   warehouse_id           varchar(255) not null,
                                   receiving_mode         varchar(255) not null,
                                   status                 varchar(255) not null,
                                   vendor_name            varchar(255) not null,
                                   expected_arrival_date  timestamp,
                                   actual_arrival_date    timestamp,

                                   constraint pk_asn primary key (id),
                                   constraint uq_asn_asn_number  unique (asn_number),
                                   constraint uq_asn_external_id unique (external_id),
                                   constraint chk_asn_receiving_mode check (receiving_mode in ('ASN_MATCHING')),
                                   constraint chk_asn_status check (status in ('EXPECTED', 'ARRIVED', 'CLOSED', 'CANCELLED'))
);

create table if not exists handling_units (
                                              id                   uuid         not null,
                                              inbound_delivery_id  uuid         not null,
                                              parent_unit_id       uuid,
                                              lpn                  varchar(255) not null,
                                              type                 varchar(255) not null,
                                              path                 varchar(255) not null,

                                              constraint pk_handling_units primary key (id),
                                              constraint uq_handling_units_lpn unique (lpn),
                                              constraint chk_handling_units_type check (type in ('DEFAULT', 'PALLET', 'BOX')),
                                              constraint fk_handling_units_inbound_delivery
                                                  foreign key (inbound_delivery_id) references asn (id),
                                              constraint fk_handling_units_parent_unit
                                                  foreign key (parent_unit_id) references handling_units (id)
);

create index if not exists idx_handling_units_inbound_delivery_id on handling_units (inbound_delivery_id);
create index if not exists idx_handling_units_parent_unit_id      on handling_units (parent_unit_id);

create table if not exists contents (
                                        id                  uuid         not null,
                                        container_unit_id   uuid         not null,
                                        sku                 varchar(255) not null,
                                        quantity            bigint       not null,

                                        constraint pk_contents primary key (id),
                                        constraint fk_contents_handling_unit
                                            foreign key (container_unit_id) references handling_units (id)
);

create index if not exists idx_contents_container_unit_id on contents (container_unit_id);

create table if not exists goods_receipts (
                                              id                   uuid         not null,
                                              asn_id               uuid         not null,
                                              manager_id           uuid         not null,
                                              gate_number          varchar(255),
                                              receiving_status     varchar(255) not null,
                                              warehouse_id         varchar(255),
                                              receiving_mode       varchar(255) not null,

                                              constraint pk_goods_receipts primary key (id)
);

create index if not exists idx_goods_receipts_asn_id     on goods_receipts (asn_id);
create index if not exists idx_goods_receipts_manager_id on goods_receipts (manager_id);

create table if not exists worker_receiving_sessions (
                                                         id                              uuid         not null,
                                                         inbound_delivery_id             uuid         not null,
                                                         receipt_id                      uuid         not null,
                                                         worker_id                       uuid         not null,
                                                         current_unit_id                 uuid,
                                                         current_unit_lpn_path           varchar(255),
                                                         receiving_mode                  varchar(255) not null,
                                                         worker_receiving_session_status varchar(255) not null,
                                                         started_at                      timestamp    not null default now(),
                                                         completed_at                    timestamp,

                                                         constraint pk_worker_receiving_sessions primary key (id),
                                                         constraint uq_worker_active_session unique (worker_id, worker_receiving_session_status)
);

create index if not exists idx_wrs_inbound_delivery_id on worker_receiving_sessions (inbound_delivery_id);
create index if not exists idx_wrs_worker_id           on worker_receiving_sessions (worker_id);
create index if not exists idx_wrs_receipt_id          on worker_receiving_sessions (receipt_id);

create table if not exists received_handling_units (
                                                       id                           uuid         not null,
                                                       parent_id                    uuid,
                                                       receipt_id                   uuid         not null,
                                                       worker_receiving_session_id  uuid         not null,
                                                       lpn                          varchar(255) not null,

                                                       constraint pk_received_handling_units primary key (id),
                                                       constraint uq_receipt_lpn unique (receipt_id, lpn),
                                                       constraint fk_rhu_parent
                                                           foreign key (parent_id) references received_handling_units (id)
);

create index if not exists idx_rhu_parent_id                  on received_handling_units (parent_id);
create index if not exists idx_rhu_receipt_id                 on received_handling_units (receipt_id);
create index if not exists idx_rhu_worker_receiving_session_id on received_handling_units (worker_receiving_session_id);

create table if not exists received_contents (
                                                 id                uuid         not null,
                                                 container_unit_id uuid,
                                                 sku               varchar(255) not null,
                                                 quantity          bigint       not null,

                                                 constraint pk_received_contents primary key (id),
                                                 constraint fk_received_contents_rhu
                                                     foreign key (container_unit_id) references received_handling_units (id)
);

create index if not exists idx_received_contents_container_unit_id on received_contents (container_unit_id);