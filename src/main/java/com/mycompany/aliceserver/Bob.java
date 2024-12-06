/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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

public class Bob {
    private static final String ALGORITHM_ASYMETIQUE = "RSA";
    private static final String ALGORITHM_SYMETIQUE = "AES";
    private static final String fonctionhachage = "HmacSHA256";
    private static KeyPair pairCleBob;
    private static PublicKey KpubA;
    private static PrivateKey KprB;
    private static final String ADDRESS_SERVEUR = "localhost";
    private static final int PORT = 2025;
    private static int tailleCle = 2048;
    
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    private static JTextArea textArea; // Ajout pour l'interface graphique

    public static void main(String[] args) throws Exception {
        
        
        // Création de l'interface graphique
        JFrame frame = new JFrame("Bob");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setVisible(true);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Génération de la paire de clés RSA pour Bob
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_ASYMETIQUE);
        keyGen.initialize(tailleCle);
        pairCleBob = keyGen.generateKeyPair();
        PublicKey clePubliqueBob = pairCleBob.getPublic();
        KprB = pairCleBob.getPrivate();

        appendText("Clés RSA générées pour Bob.\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Génération du nonce RB
        SecureRandom random = new SecureRandom();
        byte[] nonceRB = new byte[16];
        random.nextBytes(nonceRB);
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Utilisation de la fonction de hachage HMAC
        Mac hmac = Mac.getInstance(fonctionhachage);
        SecretKeySpec keySpec = new SecretKeySpec("maCleHmac".getBytes(), fonctionhachage);
        hmac.init(keySpec);
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Connexion au serveur Alice
        Socket socket = new Socket(ADDRESS_SERVEUR, PORT);
        PrintWriter entrer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader sortie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        appendText("Connecté à Alice.\n");
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Envoi de la clé publique à Alice
        entrer.println(Base64.getEncoder().encodeToString(clePubliqueBob.getEncoded()));
        appendText("Clé publique de Bob envoyée à Alice: " + Base64.getEncoder().encodeToString(clePubliqueBob.getEncoded()) + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Réception de la clé publique d'Alice
        String clePubliqueAliceBase64 = sortie.readLine();
        byte[] decodedKey = Base64.getDecoder().decode(clePubliqueAliceBase64);
        KpubA = KeyFactory.getInstance(ALGORITHM_ASYMETIQUE).generatePublic(new X509EncodedKeySpec(decodedKey));
        appendText("Clé publique d'Alice reçue: " + Base64.getEncoder().encodeToString(KpubA.getEncoded()) + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Étape 1 : Alice --- {A,KpubA(RA)} ---> Bob
        
        // Réception du message d'Alice et génération du message pour Alice
        String receptionMessageAlice01 = sortie.readLine();
        String[] parties = receptionMessageAlice01.split(",");
        byte[] encryptedRA = Base64.getDecoder().decode(parties[1]);
        appendText("Le message Reçue d'Alice ({A,KpubA(RA)}): " + receptionMessageAlice01 + "\n");
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Déchiffrement de RA
        Cipher cipher = Cipher.getInstance(ALGORITHM_ASYMETIQUE);
        cipher.init(Cipher.DECRYPT_MODE, KprB);
        byte[] nonceRA = cipher.doFinal(encryptedRA);

        // Calcul du haché de RA
        byte[] hashRA = hmac.doFinal(nonceRA);

        // Chiffrement de RB avec la clé publique d'Alice
        cipher.init(Cipher.ENCRYPT_MODE, KpubA);
        byte[] encryptedRB = cipher.doFinal(nonceRB);
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        
        // Étape 2 : Alice <--- {KpubA(RB),h(RA)} --- Bob

        // Envoi du message KpubA(RB), h(RA) à Alice
        String messagePourAlice02 = Base64.getEncoder().encodeToString(encryptedRB) + "," + Base64.getEncoder().encodeToString(hashRA);
        entrer.println(messagePourAlice02);
        appendText("Message à envoyer à Alice ({KpubA(RB),h(RA)}) : " + messagePourAlice02 + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Étape 3 : Alice --- {RB(RA(KAB))} ---> Bob
        
        //Réception de message {RB(RA(KAB))}
        String receptionMessageAlice03 = sortie.readLine();
        byte[] finalEncryptedKAB = Base64.getDecoder().decode(receptionMessageAlice03);

        // Déchiffrement avec RB
        Cipher cipherAES = Cipher.getInstance(ALGORITHM_SYMETIQUE);
        cipherAES.init(Cipher.DECRYPT_MODE, new SecretKeySpec(nonceRB, ALGORITHM_SYMETIQUE));
        byte[] chiffreKAavcRA = cipherAES.doFinal(finalEncryptedKAB);

        // Déchiffrement RA pour obtenir KAB
        cipherAES.init(Cipher.DECRYPT_MODE, new SecretKeySpec(nonceRA, ALGORITHM_SYMETIQUE));
        byte[] cleSecreteKAB = cipherAES.doFinal(chiffreKAavcRA);

        // Calcul du haché de KAB
        byte[] hashKAB = hmac.doFinal(cleSecreteKAB);
        appendText("Message envoyé pour Alice {RB(RA(KAB))}: " + receptionMessageAlice03 + "\n");
        
    /*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

        // Étape 4 : Alice <--- {KprB(h(KAB))} --- Bob
        
        // Chiffrement du haché avec la clé privée de Bob
        cipher.init(Cipher.ENCRYPT_MODE, KprB);
        byte[] signHachKAB = cipher.doFinal(hashKAB);
        String messagePourAlice4 = Base64.getEncoder().encodeToString(signHachKAB);
        entrer.println(messagePourAlice4);
        appendText("Message à envoyer à Alice {KprB(h(KAB)} : " + messagePourAlice4 + "\n");

        //Fermeture de la connexions
        socket.close();
        
        
        
    }
    
    // Méthode pour ajouter du texte à JTextArea de manière thread-safe
    private static void appendText(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}
