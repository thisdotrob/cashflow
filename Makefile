.PHONY: encrypt-user-data

encrypt-user-data:
	gpg --symmetric < user_data/adjustments.json > adjustments.json.enc
	gpg --symmetric < user_data/one_off.json > one_off.json.enc
	gpg --symmetric < user_data/recurring.json > recurring.json.enc

decrypt-user-data:
	gpg --decrypt < adjustments.json.enc > user_data/adjustments.json
	gpg --decrypt < one_off.json.enc > user_data/one_off.json
	gpg --decrypt < recurring.json.enc > user_data/recurring.json
