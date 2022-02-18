ALTER TABLE game_metadata MODIFY COLUMN game VARCHAR(32);
START TRANSACTION;
UPDATE game_metadata SET game = 'rps' WHERE game = '0';
UPDATE game_metadata SET game = 'roulette' WHERE game = '1';
UPDATE game_metadata SET game = 'dice' WHERE game = '2';
COMMIT;