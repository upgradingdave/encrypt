### Encryption and Decryption

This library provides an easy way to encrypt and decrypt strings. This
is useful for times when you need to store a password inside a
database or config file.

The `upgrade.encrypt.EncryptionManager` class provides the following methods: 

1. generateKeyFile
2. encrypt
3. decrypt

A `keyfile` is a plain text file with a secret passphrase used to
encrypt data.

So, the first step is to generate a keyfile like this:

	java -jar ./encrypt/target/encrypt.jar -f conf/keyfile -g
	
Now, you can use that keyfile to encrypt and decrypt messages: 

	java -jar ./encrypt/target/encrypt.jar -f conf/keyfile -e "Super Secret"
	=> Attempting to encrypt message: 'Super Secret'
	=> AAAADE1YRDlDuhAqbXcHZ4xNjXqcG63GQxKRcfnTJRvf0mBtYfHUwd/chzg=
	
	java -jar ./encrypt/target/encrypt.jar -f conf/keyfile -d AAAADE1YRDlDuhAqbXcHZ4xNjXqcG63GQxKRcfnTJRvf0mBtYfHUwd/chzg=
	=> Super Secret

### Creating the Command Line Executable Jar

You can encrypt and decrypt from the command line. Each time you build
this project with maven, it should create an executable jar file named
`encrypt/target/encrypt.jar`. You can run this file like so: 

	java -jar ./encrypt/target/encrypt.jar -h
	
### See Unit Tests

If you're interested in encrypting and/or decrypting code from within
your java code, Please look inside
`src/test/upgrade/encrypt/EncryptionManagerTest` for examples.


