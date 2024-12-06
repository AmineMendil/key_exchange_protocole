# 1- Description du Travail Réalisé :
Dans ce projet, j'ai créé deux classes : AliceServeur et Bob. La classe AliceServeur implémente un serveur utilisant une socket TCP, tandis que la classe Bob représente un client qui se connecte à ce serveur via le port 2025.

# 2- Objectif du Travail :
L'objectif de ce projet est de mettre en œuvre un protocole d'échange d'une clé symétrique générée à l'aide de l'algorithme de chiffrement AES, en utilisant des nonces et des clés publiques générées par l'algorithme de cryptographie asymétrique RSA.

# 3- Étapes de fonctionnement du protocole :

  ### Alice :
  + Génère une clé publique KpubAKpubA​ et une clé privée KprAKprA​ à l'aide de l'algorithme RSA. 
  + Crée un nonce RARA​.
  + Utilise une fonction de hachage HMAC.
  + Génère une clé secrète KABKAB​ à l'aide de l'algorithme de chiffrement AES.

  ### Bob :
   + Génère une clé publique KpubBKpubB​ et une clé privée KprBKprB​ à l'aide de l'algorithme RSA.
   + Crée un nonce RBRB​.
   + Utilise une fonction de hachage HMAC.

# 4- Étapes suivies par le protocole :

  ### Échange initial des clés publiques :
  Les deux entités échangent leurs clés publiques KpubAKpubA​ et KpubBKpubB​ dès qu'un client se connecte au serveur.

  #### Étape 1 : Alice --- {A,Kpub(RA)} ---> Bob
  Alice :
   + Chiffre son nonce RARA​ avec la clé publique de Bob KpubBKpubB​.
   + Concatène son identité avec le nonce chiffré A,KpubB(RA)A,KpubB​(RA​).
   + Envoie le résultat à Bob.

  #### Étape 2 : Alice <--- {KpubA(RB),h(RA)} --- Bob
  Bob :
   + Reçoit le message d'Alice.
   + Extrait KpubB(RA)KpubB​(RA​).
   + Déchiffre RARA​ à l'aide de sa clé privée KprBKprB​.
   + Calcule le haché de RARA​ : h(RA)h(RA​).
   + Chiffre son nonce RBRB​ avec la clé publique d'Alice KpubAKpubA​.
   + Concatène KpubA(RB)KpubA​(RB​) et h(RA)h(RA​) pour obtenir KpubA(RB),h(RA)KpubA​(RB​),h(RA​).
   + Envoie ce message à Alice.

  #### Étape 3 : Alice --- {RB(RA(KAB))} ---> Bob
  Alice :
   + Reçoit le message de Bob.
   + Extrait et déchiffre KpubA(RB)KpubA​(RB​) à l'aide de sa clé privée KprAKprA​ pour obtenir RBRB​.
   + Compare h(RA)h(RA​) reçu avec le haché recalculé localement pour RARA​.
    Si les hachés correspondent, l'opération est validée. Sinon, une erreur est signalée.
     Chiffre la clé secrète KABKAB​ avec RARA​ en utilisant AES : AESRA(KAB)AESRA​​(KAB​).
     Chiffre le résultat obtenu avec RBRB​ toujours en AES : AESRB(AESRA(KAB))AESRB​​(AESRA​​(KAB​)).
     Envoie le résultat à Bob.

  #### Étape 4 : Alice <--- {KprB(h(KAB))} --- Bob
   Bob :
    + Reçoit le message d'Alice.
    + Déchiffre avec RBRB​ à l'aide de l'AES : AESRA(KAB)AESRA​​(KAB​).
    + Déchiffre à nouveau avec RARA​ pour obtenir KABKAB​.
    + Calcule le haché de KABKAB​.
    + Chiffre le haché avec sa clé privée KprB : SigKprB​​(h(KAB​).
    + Envoie le résultat à Alice.

  #### Étape 5 : vérification de la clé secrète KAB
   Alice :
    + Déchiffre le message reçu avec la clé publique de Bob KpubBKpubB​ pour obtenir h(KAB​).
    + Hache la clé KAB localement.
    + Compare le haché calculé avec celui reçu.
       Si les deux sont identiques, elle envoie un message de réussite à Bob.
       Sinon, elle envoie un message d'erreur.
# 1- Description du Travail Réalisé :
Dans ce projet, j'ai créé deux classes : AliceServeur et Bob. La classe AliceServeur implémente un serveur utilisant une socket TCP, tandis que la classe Bob représente un client qui se connecte à ce serveur via le port 2025.

# 2- Objectif du Travail :
L'objectif de ce projet est de mettre en œuvre un protocole d'échange d'une clé symétrique générée à l'aide de l'algorithme de chiffrement AES, en utilisant des nonces et des clés publiques générées par l'algorithme de cryptographie asymétrique RSA.

# 3- Étapes de fonctionnement du protocole :

  ### Alice :
  + Génère une clé publique KpubAKpubA​ et une clé privée KprAKprA​ à l'aide de l'algorithme RSA. 
  + Crée un nonce RARA​.
  + Utilise une fonction de hachage HMAC.
  + Génère une clé secrète KABKAB​ à l'aide de l'algorithme de chiffrement AES.

  ### Bob :
   + Génère une clé publique KpubBKpubB​ et une clé privée KprBKprB​ à l'aide de l'algorithme RSA.
   + Crée un nonce RBRB​.
   + Utilise une fonction de hachage HMAC.

# 4- Étapes suivies par le protocole :

  ### Échange initial des clés publiques :
  Les deux entités échangent leurs clés publiques KpubAKpubA​ et KpubBKpubB​ dès qu'un client se connecte au serveur.

  #### Étape 1 : Alice --- {A,Kpub(RA)} ---> Bob
  Alice :
   + Chiffre son nonce RARA​ avec la clé publique de Bob KpubBKpubB​.
   + Concatène son identité avec le nonce chiffré A,KpubB(RA)A,KpubB​(RA​).
   + Envoie le résultat à Bob.

  #### Étape 2 : Alice <--- {KpubA(RB),h(RA)} --- Bob
  Bob :
   + Reçoit le message d'Alice.
   + Extrait KpubB(RA)KpubB​(RA​).
   + Déchiffre RARA​ à l'aide de sa clé privée KprBKprB​.
   + Calcule le haché de RARA​ : h(RA)h(RA​).
   + Chiffre son nonce RBRB​ avec la clé publique d'Alice KpubAKpubA​.
   + Concatène KpubA(RB)KpubA​(RB​) et h(RA)h(RA​) pour obtenir KpubA(RB),h(RA)KpubA​(RB​),h(RA​).
   + Envoie ce message à Alice.

  #### Étape 3 : Alice --- {RB(RA(KAB))} ---> Bob
  Alice :
   + Reçoit le message de Bob.
   + Extrait et déchiffre KpubA(RB)KpubA​(RB​) à l'aide de sa clé privée KprAKprA​ pour obtenir RBRB​.
   + Compare h(RA)h(RA​) reçu avec le haché recalculé localement pour RARA​.
    Si les hachés correspondent, l'opération est validée. Sinon, une erreur est signalée.
     Chiffre la clé secrète KABKAB​ avec RARA​ en utilisant AES : AESRA(KAB)AESRA​​(KAB​).
     Chiffre le résultat obtenu avec RBRB​ toujours en AES : AESRB(AESRA(KAB))AESRB​​(AESRA​​(KAB​)).
     Envoie le résultat à Bob.

  #### Étape 4 : Alice <--- {KprB(h(KAB))} --- Bob
   Bob :
    + Reçoit le message d'Alice.
    + Déchiffre avec RBRB​ à l'aide de l'AES : AESRA(KAB)AESRA​​(KAB​).
    + Déchiffre à nouveau avec RARA​ pour obtenir KABKAB​.
    + Calcule le haché de KABKAB​.
    + Chiffre le haché avec sa clé privée KprB : SigKprB​​(h(KAB​).
    + Envoie le résultat à Alice.

  #### Étape 5 : vérification de la clé secrète KAB
   Alice :
    + Déchiffre le message reçu avec la clé publique de Bob KpubBKpubB​ pour obtenir h(KAB​).
    + Hache la clé KAB localement.
    + Compare le haché calculé avec celui reçu.
       Si les deux sont identiques, elle envoie un message de réussite à Bob.
       Sinon, elle envoie un message d'erreur.
