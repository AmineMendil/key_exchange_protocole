/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.aliceserver;



import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.net.*;
import java.io.*;
import java.util.Base64;
import java.util.Arrays;

public class AliceServer {
    private static final String ALGORITHME_ASYMETRIQUE = "RSA";
    private static final String ALGORITHM_SYMETRIQUE = "AES";
    private static final String fonctionhachage = "HmacSHA256";
    private static KeyPair pairCleAlice;
    private static PublicKey KpubB;
    private static PrivateKey KprA;
    private static SecretKey cleSecreteKAB;
    private static int tailleCle = 2048;
    private static final int PORT = 2025;

    private static JTextArea textArea; // Ajout pour l'interface graphique

    public static void main(String[] args) throws Exception {
        
        
        
        // Création de l'interface graphique
        JFrame frame = new JFrame("Alice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setVisible(true);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Génération de la paire de clés RSA pour Alice
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHME_ASYMETRIQUE);
        keyGen.initialize(tailleCle);
        pairCleAlice = keyGen.generateKeyPair();
        PublicKey clePubliqueAlice = pairCleAlice.getPublic();
        KprA = pairCleAlice.getPrivate();

        appendText("Clés RSA générées pour Alice.\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Génération du nonce RA
        SecureRandom random = new SecureRandom();
        byte[] nonceRA = new byte[16];
        random.nextBytes(nonceRA);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Utilisation de la fonction de hachage HMAC
        Mac hmac = Mac.getInstance(fonctionhachage);
        SecretKeySpec keySpec = new SecretKeySpec("maCleHmac".getBytes(), fonctionhachage);
        hmac.init(keySpec);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Création du serveur TCP
        ServerSocket serverSocket = new ServerSocket(PORT);
        appendText("Alice attend les connexions sur le port " + PORT + "\n");

        Socket clientSocket = serverSocket.accept();
        appendText("Connexion acceptée de " + clientSocket.getInetAddress() + "\n");

        PrintWriter sortie = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader entrer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Envoi de la clé publique à Bob
        sortie.println(Base64.getEncoder().encodeToString(clePubliqueAlice.getEncoded()));
        appendText("Clé publique d'Alice envoyée à Bob: " + Base64.getEncoder().encodeToString(clePubliqueAlice.getEncoded()) + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Réception de la clé publique de Bob
        String clePubliqueBobBase64 = entrer.readLine();
        byte[] decodedKey = Base64.getDecoder().decode(clePubliqueBobBase64);
        KpubB = KeyFactory.getInstance(ALGORITHME_ASYMETRIQUE).generatePublic(new X509EncodedKeySpec(decodedKey));
        appendText("Clé publique de Bob reçue: " + Base64.getEncoder().encodeToString(KpubB.getEncoded()) + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        
        // Étape 1 : Alice --- {A,Kpub(RA)} ---> Bob
        
        // Envoi de A, KpubB(RA) à Bob
        Cipher cipher = Cipher.getInstance(ALGORITHME_ASYMETRIQUE);
        cipher.init(Cipher.ENCRYPT_MODE, KpubB);
        byte[] encryptedRA = cipher.doFinal(nonceRA);
        String messagPourBob1 = "Alice," + Base64.getEncoder().encodeToString(encryptedRA);
        sortie.println(messagPourBob1);
        appendText("Message à envoyer à Bob {A,KpubB(RA)}: " + messagPourBob1 + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        
        // Étape 2 : Alice <--- {KpubA(RB),h(RA)} --- Bob
        
        // Réception du message de Bob et vérification du haché de RA
        String MessageDeBob = entrer.readLine();
        String[] parts = MessageDeBob.split(",");
        byte[] encryptedRB = Base64.getDecoder().decode(parts[0]);
        byte[] HashDeRA = Base64.getDecoder().decode(parts[1]);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Déchiffrement de RB
        cipher.init(Cipher.DECRYPT_MODE, KprA);
        byte[] nonceRB = cipher.doFinal(encryptedRB);

        // Calcul du haché de RA et vérification
        byte[] localHashRA = hmac.doFinal(nonceRA);
        appendText("Message recue de Bob {KpubA(RB),h(RA)}: " + MessageDeBob + "\n");
        
        /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        
        // Etape 3 : Alice <--- {RB(RA(KAB))} --- Bob
        
        if (Arrays.equals(HashDeRA, localHashRA)) {
            appendText("RA vérifié avec succès.\n");

            // Génération de la clé secrète KAB avec AES
            KeyGenerator keyGenAES = KeyGenerator.getInstance(ALGORITHM_SYMETRIQUE);
            keyGenAES.init(128);
            cleSecreteKAB = keyGenAES.generateKey();

            // Chiffrement de KAB avec RA et RB
            Cipher cipherAES = Cipher.getInstance(ALGORITHM_SYMETRIQUE);
            cipherAES.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(nonceRA, ALGORITHM_SYMETRIQUE));
            byte[] encryptedKABWithRA = cipherAES.doFinal(cleSecreteKAB.getEncoded());

            cipherAES.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(nonceRB, ALGORITHM_SYMETRIQUE));
            byte[] finalEncryptedKAB = cipherAES.doFinal(encryptedKABWithRA);
            String messagePourBob3 = Base64.getEncoder().encodeToString(finalEncryptedKAB);
            sortie.println(messagePourBob3);
            appendText("Message à envoyer à Bob {RB(RA(KAB))} : " + messagePourBob3 + "\n");
        } else {
            appendText("Erreur de vérification de RA.\n");
        }

    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Étape 4 : Alice <--- {KprB(h(KAB))} --- Bob
        
        // Étape 4 : Réception de la signature de Bob
        String signatureFromBob = entrer.readLine();
        byte[] receivedSignature = Base64.getDecoder().decode(signatureFromBob);

        // Vérification de la signature de Bob
        cipher.init(Cipher.DECRYPT_MODE, KpubB);
        byte[] receivedHashKAB = cipher.doFinal(receivedSignature);

        // Calcul du haché de KAB
        byte[] localHashKAB = hmac.doFinal(cleSecreteKAB.getEncoded());

        if (Arrays.equals(receivedHashKAB, localHashKAB)) {
            appendText("La clé KAB est vérifiée avec succès: " + Base64.getEncoder().encodeToString(cleSecreteKAB.getEncoded()) + "\n");
        } else {
            appendText("Erreur de vérification de KAB.\n");
        }

        // Fermeture des connexions
        clientSocket.close();
        serverSocket.close();
        
        
    }

    // Méthode pour ajouter du texte à JTextArea de manière thread-safe
    private static void appendText(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}

