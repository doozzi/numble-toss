create table account (
                         id bigint not null auto_increment,
                         balance decimal(19,2),
                         number varchar(14),
                         bank_id bigint,
                         member_id bigint,
                         primary key (id)
) engine=InnoDB;

create table account_stocks (
                                account_id bigint not null,
                                stocks_id bigint not null,
                                primary key (account_id, stocks_id)
) engine=InnoDB;
create table account_trades (
                                account_id bigint not null,
                                trades_id bigint not null,
                                primary key (account_id, trades_id)
) engine=InnoDB;
create table account_stock (
                               id bigint not null auto_increment,
                               balance decimal(19,2),
                               price decimal(19,2),
                               total_paid decimal(19,2),
                               stock_id bigint,
                               primary key (id)
) engine=InnoDB;
create table account_stock_history (
                                       account_stock_id bigint not null,
                                       history_id bigint not null,
                                       primary key (account_stock_id, history_id)
) engine=InnoDB;
create table account_stock_trade (
                                     id bigint not null auto_increment,
                                     amount decimal(19,2),
                                     price decimal(19,2),
                                     trade_at datetime(6),
                                     trade_type varchar(255),
                                     primary key (id)
) engine=InnoDB;
create table authority (
                           authority_name varchar(50) not null,
                           primary key (authority_name)
) engine=InnoDB;
create table bank (
                      id bigint not null auto_increment,
                      name varchar(255),
                      number bigint,
                      primary key (id)
) engine=InnoDB;
create table member (
                        id bigint not null auto_increment,
                        nickname varchar(50),
                        password varchar(100),
                        username varchar(50),
                        primary key (id)
) engine=InnoDB;
create table member_authority (
                                  member_id bigint not null,
                                  authority_name varchar(50) not null,
                                  primary key (member_id, authority_name)
) engine=InnoDB;
create table stock (
                       id bigint not null auto_increment,
                       balance decimal(19,2),
                       name varchar(255),
                       price decimal(19,2),
                       primary key (id)
) engine=InnoDB;
create table stock_histories (
                                 stock_id bigint not null,
                                 histories_id bigint not null,
                                 primary key (stock_id, histories_id)
) engine=InnoDB;
create table stock_history (
                               id bigint not null auto_increment,
                               price decimal(19,2),
                               written_at datetime(6),
                               primary key (id)
) engine=InnoDB;
create table token (
                       id bigint not null auto_increment,
                       expiration datetime(6),
                       refresh_token varchar(255),
                       member_id bigint,
                       primary key (id)
) engine=InnoDB;
create table trade (
                       id bigint not null auto_increment,
                       amount decimal(19,2),
                       date_time datetime(6),
                       type varchar(255),
                       primary key (id)
) engine=InnoDB;
create table trade_reservation (
                                   id bigint not null auto_increment,
                                   amount decimal(19,2),
                                   status varchar(255),
                                   trade_at datetime(6),
                                   account_id bigint,
                                   primary key (id)
) engine=InnoDB;
alter table account
    add constraint UK_dbfiubqahb32ns85k023gr6nn unique (number);
alter table account_stocks
    add constraint UK_g7bspl8urrbp8i2fd4jmtdciu unique (stocks_id);
alter table account_trades
    add constraint UK_ot3qs83mwy0q55xkofgn3ugs2 unique (trades_id);
alter table account_stock_history
    add constraint UK_lu4a7j6bp9ppka17icpygw6uw unique (history_id);
alter table member
    add constraint UK_gc3jmn7c2abyo3wf6syln5t2i unique (username);
alter table stock_histories
    add constraint UK_ef7y7oc8s5h5mcedd47a28cdl unique (histories_id);
alter table account
    add constraint FKawl1lrpngb7h5ktg79odeic5w
        foreign key (bank_id)
            references bank (id);
alter table account
    add constraint FKr5j0huynd7nsv1s7e9vb8qvwo
        foreign key (member_id)
            references member (id);
alter table account_stocks
    add constraint FK25fup71j6mr99xjwlgvo71mg4
        foreign key (stocks_id)
            references account_stock (id);
alter table account_stocks
    add constraint FK88lhn7hvycopa1bd0btfw298n
        foreign key (account_id)
            references account (id);
alter table account_trades
    add constraint FKtkkpd3uf03ssp3gf9y4uw8plv
        foreign key (trades_id)
            references trade (id);
alter table account_trades
    add constraint FKahmc68xx9ae8h98g6yn3gv73q
        foreign key (account_id)
            references account (id);
alter table account_stock
    add constraint FK3wgf1ng3nu77q36stglvvu0mo
        foreign key (stock_id)
            references stock (id);

alter table account_stock_history
    add constraint FKcxqx5fnvo4l4j0prxerp3hxig
        foreign key (history_id)
            references account_stock_trade (id);
alter table account_stock_history
    add constraint FKnvp0j9sa4e21v3xq2k18i48i4
        foreign key (account_stock_id)
            references account_stock (id);
alter table member_authority
    add constraint FKasnmjar8jr5gaxvd7966p19ir
        foreign key (authority_name)
            references authority (authority_name);
alter table member_authority
    add constraint FK8uf5ff5jr0nuvbj4yfp5ob9sq
        foreign key (member_id)
            references member (id);
alter table stock_histories
    add constraint FKk5v7xn3ljfjer67xf6u6n5exc
        foreign key (histories_id)
            references stock_history (id);
alter table stock_histories
    add constraint FKgqolollvws7nwb8eav9ekyyg
        foreign key (stock_id)
            references stock (id);
alter table token
    add constraint FK8a0sdl451qcw4ishfaxpdog0p
        foreign key (member_id)
            references member (id);
alter table trade_reservation
    add constraint FKqvxm26cxqftwkt72q54qu7jqw
        foreign key (account_id)
            references account (id);

-- Autogenerated: do not edit this file

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME DATETIME(6) NOT NULL,
	START_TIME DATETIME(6) DEFAULT NULL ,
	END_TIME DATETIME(6) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED DATETIME(6),
	JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL ,
	STRING_VAL VARCHAR(250) ,
	DATE_VAL DATETIME(6) DEFAULT NULL ,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME DATETIME(6) NOT NULL ,
	END_TIME DATETIME(6) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED DATETIME(6),
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);
