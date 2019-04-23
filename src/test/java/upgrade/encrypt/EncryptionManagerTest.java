package upgrade.encrypt;

import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertEquals;

public class EncryptionManagerTest {

    @Test
    public void encryptDecrypt() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {

        File keyFile = File.createTempFile("encrypt-test", ".tmp");
        EncryptionManager encryptionManager = new EncryptionManager();
        encryptionManager.generateKeyFile(keyFile);

        String strToEncrypt = "This is a super secret message";
        String encrypted = encryptionManager.encrypt(keyFile, strToEncrypt);
        String decryptedStr = encryptionManager.decrypt(keyFile, encrypted);
        assertEquals(strToEncrypt, decryptedStr);
    }
}
