package com.glsl.security;

import com.badlogic.gdx.files.FileHandle;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils{

        private static final String ALGORITHM = "AES";
        private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
        private static final byte[] salt = "[B@2a56acbe".getBytes();
        private static final byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        public static void encrypt(String key, FileHandle file)
                throws EncryptException {
            doCrypto(Cipher.ENCRYPT_MODE, key, file, file);
        }

        public static String encrypt(String key, FileHandle inputFile, FileHandle outputFile)
                throws EncryptException {
            return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
        }

        public static String encrypt(String key, String textToEncrypt, FileHandle outputFile)
                throws EncryptException {
            return doCrypto(Cipher.ENCRYPT_MODE, key, textToEncrypt, outputFile);
        }

        public static String decrypt(String key, FileHandle inputFile, FileHandle outputFile)
                throws EncryptException {
            return doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
        }

        public static String decrypt(String key, FileHandle inputFile)
                throws EncryptException {
            return doCrypto(Cipher.DECRYPT_MODE, key, inputFile, null);
        }

        public static String decrypt(String key, String inputFile)
                throws EncryptException {
            return doCrypto(Cipher.DECRYPT_MODE, key, inputFile, null);
        }

        private static String doCrypto(int cipherMode, String key, FileHandle inputFile,
                                     FileHandle outputFile) throws EncryptException {
            try {
                SecretKeyFactory factory = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536,
                        128);
                SecretKey secretKey = factory.generateSecret(keySpec);
                SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
                IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(cipherMode, secret, ivspec);

                byte[] outputBytes;
                byte[] inputBytes = inputFile.readBytes().clone();
                if(cipherMode == Cipher.ENCRYPT_MODE){
                    outputBytes = cipher.doFinal(inputBytes);
                    if(outputFile != null) outputFile.writeString(Hex.encodeHexString(outputBytes), false);
                }else{
                    char[] chars = inputFile.readString().toCharArray().clone();
                    outputBytes = cipher.doFinal(Hex.decodeHex(chars));
                }

                return new String(outputBytes);

            } catch (NoSuchPaddingException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (InvalidKeyException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (BadPaddingException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (IllegalBlockSizeException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            return null;
        }

        private static String doCrypto(int cipherMode, String key, String text,
                                     FileHandle outputFile) throws EncryptException {
            try {
                SecretKeyFactory factory = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536,
                        128);
                SecretKey secretKey = factory.generateSecret(keySpec);
                SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
                IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(cipherMode, secret, ivspec);

                byte[] outputBytes;
                if(cipherMode == Cipher.ENCRYPT_MODE) {
                    byte[] inputBytes = text.getBytes().clone();
                    outputBytes = cipher.doFinal(inputBytes);
                    if(outputFile != null) outputFile.writeString(Hex.encodeHexString(outputBytes), false);
                }else{
                    char[] chars = text.toCharArray().clone();
                    outputBytes = cipher.doFinal(Hex.decodeHex(chars));
                }

                return new String(outputBytes);

            } catch (NoSuchPaddingException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (InvalidKeyException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (BadPaddingException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (IllegalBlockSizeException ex) {
                throw new EncryptException("Error encrypting/decrypting file", ex);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            return null;
        }
}