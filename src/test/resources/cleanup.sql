SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE account;
TRUNCATE TABLE account_stock;
TRUNCATE TABLE account_stock_trade;
TRUNCATE TABLE account_stock_history;
TRUNCATE TABLE authority;
TRUNCATE TABLE bank;
TRUNCATE TABLE member;
TRUNCATE TABLE stock;
TRUNCATE TABLE stock_history;
TRUNCATE TABLE trade;
TRUNCATE TABLE trade_reservation;
truncate table member_authority;
truncate table account_trades;
truncate table stock_history;
truncate table stock_histories;
truncate table account_stocks;

alter table account alter column id restart with 1;
alter table account_stock alter column id restart with 1;
alter table account_stock_trade alter column id restart with 1;
alter table bank alter column id restart with 1;
alter table member alter column id restart with 1;
alter table stock alter column id restart with 1;
alter table stock_history alter column id restart with 1;
alter table trade alter column id restart with 1;
alter table trade_reservation alter column id restart with 1;
alter table stock_history alter column id restart with 1;

SET REFERENTIAL_INTEGRITY TRUE;