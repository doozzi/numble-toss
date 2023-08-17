insert into member (username, password, nickname) values ('admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin');
insert into member (username, password, nickname) values ('member', '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'member');

insert into authority (authority_name) values ('ROLE_USER');
insert into authority (authority_name) values ('ROLE_ADMIN');

insert into member_authority (member_id, authority_name) values (1, 'ROLE_USER');
insert into member_authority (member_id, authority_name) values (1, 'ROLE_ADMIN');
insert into member_authority (member_id, authority_name) values (2, 'ROLE_USER');

insert into bank(name, number) values ('은행', 1);

insert into account(number, bank_id, member_id, balance) values('1234', 1, 1, 1000);
insert into account(number, bank_id, member_id, balance) values('1235', 1, 2, 1000);

insert into trade(type, date_time, amount) values('DEPOSIT', '2023-07-01T11:25:00', 1000);
insert into trade(type, date_time, amount) values('WITHDRAW', '2023-07-02T01:25:30', 1000);
insert into trade(type, date_time, amount) values('PAYMENT', '2023-07-03T17:55:00', 1000);

insert into trade_reservation(amount, status, account_id, trade_at) values(100, 'READY', 2, '2023-07-15T17:00:00');
insert into trade_reservation(amount, status, account_id, trade_at) values(100, 'READY', 2, '2023-07-15T17:00:00');
insert into trade_reservation(amount, status, account_id, trade_at) values(100, 'READY', 2, '2023-07-15T17:00:00');

insert into stock(id, balance, name, price) values(10, 100, '천만전자', 11100);
insert into stock(balance, name, price) values(100, '백만전자', 100);
insert into stock(balance, name, price) values(100, '십만전자', 100);
insert into stock(balance, name, price) values(100, '화장품회사', 10000);
insert into stock(balance, name, price) values(100, '만전자', 10000);
insert into stock(balance, name, price) values(100, '천전자', 1500);
insert into stock(balance, name, price) values(100, '백전자', 1000);
insert into stock(id, balance, name, price) values(100, 100, '핸드폰회사', 2100);
insert into stock(id, balance, name, price) values(101, 100, '컴퓨터회사', 3100);
insert into stock(id, balance, name, price) values(102, 100, '피자회사', 5100);
insert into stock(id, balance, name, price) values(103, 100, '과자회사', 9100);

insert into account_trades(account_id, trades_id) values(1, 1);
insert into account_trades(account_id, trades_id) values(1, 2);
insert into account_trades(account_id, trades_id) values(1, 3);

insert into account_stock(balance, price, total_paid, stock_id) values(10000, 1000, 10000000, 1);
insert into account_stocks(account_id, stocks_id) values (1, 1);

insert into stock_history(price, written_at) values (80, '2023-07-01T13:00:00');
insert into stock_history(price, written_at) values (90, '2023-07-01T14:00:00');
insert into stock_histories(stock_id, histories_id) values (1, 1);
insert into stock_histories(stock_id, histories_id) values (1, 2);