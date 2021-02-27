package open.jp;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA 
{
	private static PublicKey publicKey ;/* 公钥 */
	private static PrivateKey privateKey ;/* 私钥 */
	
	public static void generateKey() throws Exception
	{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		
		keyPairGenerator.initialize(1024);/* 默认密钥模长度设置为1024 */
		
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		
		publicKey = keyPair.getPublic();
		
		privateKey = keyPair.getPrivate();
		
		System.out.println(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		
		System.out.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
	}
	
	public static byte[] encryptPublicKey(String message) throws Exception
	{
		//通过X509编码的Key指令获得公钥对象
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		
		PublicKey keyFactoryPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);
		
		Cipher cipher = Cipher.getInstance("RSA");
		
		cipher.init(Cipher.ENCRYPT_MODE, keyFactoryPublicKey);
		
		byte[] bytes = cipher.doFinal(message.getBytes());
		
		System.out.println("公钥加密后：" + Base64.getEncoder().encodeToString(bytes));
		
		return bytes ;
	}
	
	
	public static void decryptPrivateKey(byte[] messages) throws Exception
	{
		//通过PKCS#8编码的Key指令获得私钥对象
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		
		PrivateKey keyFactoryPrivateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		
		Cipher cipher = Cipher.getInstance("RSA");
		
		cipher.init(Cipher.DECRYPT_MODE, keyFactoryPrivateKey);
		
		byte[] bytes = cipher.doFinal(messages);
		
		System.out.println("私钥解密后：" + new String(bytes));
	}
}