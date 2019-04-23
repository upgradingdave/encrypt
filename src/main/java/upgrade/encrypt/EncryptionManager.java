package upgrade.encrypt;

import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class EncryptionManager {

  private static String ALGORITHM = "AES";
  private static String CIPHER = "AES/GCM/NoPadding";

  public void generateKeyFile(File keyFile) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {


    SecretKey key = KeyGenerator.getInstance(ALGORITHM).generateKey();
    byte[] encoded = key.getEncoded();

    FileOutputStream fos = new FileOutputStream(keyFile);

    fos.write(encoded);
    fos.close();
  }

  public SecretKey readKey(File keyFile) throws IOException {
    byte[] encoded = FileUtils.readFileToByteArray(keyFile);
    return new SecretKeySpec(encoded, ALGORITHM);
  }

  public String encrypt(File keyFile, String stringToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
          IOException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

    SecretKey key = readKey(keyFile);

    byte[] iv = new byte[12]; //NEVER REUSE THIS IV WITH SAME KEY
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(CIPHER);

    GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

    byte[] cipherText = cipher.doFinal(stringToEncrypt.getBytes());

    ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
    byteBuffer.putInt(iv.length);
    byteBuffer.put(iv);
    byteBuffer.put(cipherText);
    byte[] cipherMessage = byteBuffer.array();
    return new String(Base64.encodeBase64(cipherMessage));
  }

  public String decrypt(File keyFile, String stringToDecrypt) throws IOException, NoSuchPaddingException,
          NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
          IllegalBlockSizeException {

    byte[] cipherMessage = Base64.decodeBase64(stringToDecrypt);
    ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
    int ivLength = byteBuffer.getInt();
    if(ivLength < 12 || ivLength >= 16) { // check input parameter
      throw new IllegalArgumentException("invalid iv length");
    }
    byte[] iv = new byte[ivLength];
    byteBuffer.get(iv);
    byte[] cipherText = new byte[byteBuffer.remaining()];
    byteBuffer.get(cipherText);

    SecretKey key = readKey(keyFile);

    final Cipher cipher = Cipher.getInstance(CIPHER);
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
    byte[] plainText= cipher.doFinal(cipherText);
    return new String(plainText);

  }

  /**
   * This can be used to encrypt or decrypt strings. This can be useful if you need to put a password inside of a
   * config file, for example. In order to encrypt a string, you first need to generate a `keyfile` that contains
   * the secret passphrase used for encrypting and decrypting.
   *
   * To get started, try running this from the commandline to read the help
   *
   * @param args command line args
   * @throws Exception any problems parsing commandline arguments will throw an exception
   */
  public static void main (String[] args) throws Exception {

    // setup command line options
    Options options = new Options();
    Option help = new Option("h", "help", false, "print this message" );
    options.addOption(help);

    Option keyFilePath = new Option("f", "keyfile", true, "(required) Path to an existing key file, or, when generating new key file, this will specify where to create the new key file");
    keyFilePath.setArgName("key-file-path");
    keyFilePath.setRequired(true);
    options.addOption(keyFilePath);

    Option genKeyFile = new Option("g", "genkeyfile", false, "Generate a new Secret Key File");
    options.addOption(genKeyFile);

    Option encrypt = new Option("e", "encrypt", true, "Encrypt a message into base64 encoded byte string");
    encrypt.setArgName("message-to-encrypt");
    options.addOption(encrypt);

    Option decrypt = new Option("d", "decrypt", true, "Decrypt string a base64 encoded byte string into original message");
    decrypt.setArgName("string-to-decrypt");
    options.addOption(decrypt);

    CommandLineParser parser = new DefaultParser();
    try {
      boolean doneSomething = false;
      CommandLine cmd = parser.parse( options, args);

      if(cmd.hasOption("h")) {
        help(options);
        doneSomething = true;
      }

      String filePath = cmd.getOptionValue("f");
      File keyFile = new File(filePath);
      EncryptionManager encryptionManager = new EncryptionManager();

      // Generate Key File
      if(cmd.hasOption("g")) {

        System.out.println("Attempting to generate secret key file '"+filePath+"'");

        try {
          encryptionManager.generateKeyFile(keyFile);
        } catch (Exception e) {
          System.out.println("Unable to create secret key file, "+  e.getMessage());
          throw e;
        }
        doneSomething = true;
      }

      // Encrypt
      if(cmd.hasOption("e")) {

        String message = cmd.getOptionValue("e");
        System.out.println("Attempting to encrypt message: '"+message+"'");

        try {
          System.out.println(encryptionManager.encrypt(keyFile, message));
        } catch (Exception e) {
          System.out.println("Unable to encrypt, "+  e.getMessage());
          throw e;
        }
        doneSomething = true;
      }

      // Decrypt
      if(cmd.hasOption("d")) {

        String message = cmd.getOptionValue("d");
        System.out.println("Attempting to decrypt: '"+message+"'");

        try {
          System.out.println(encryptionManager.decrypt(keyFile, message));
        } catch (Exception e) {
          System.out.println("Unable to decrypt, "+  e.getMessage());
          throw e;
        }
        doneSomething = true;
      }

      if(!doneSomething) {
        help(options);
      }

    } catch (ParseException e) {
      help(options);
    }
  }

  private static void help(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "java -jar pw-encrypt.jar", options );
  }

}
