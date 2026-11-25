# CERAMICA POS — Version PC Navigateur (Synchronisée avec Android)

Cette version PC fonctionne dans votre navigateur (**Chrome / Edge / Firefox**) et se synchronise **en temps réel** avec tous vos téléphones/tablettes Android via Firebase (`ace-project-498620`, base `ai-studio-android-82d1216f-37fc-468a-a33f-aa7c2903aaec`).

## Lancement sur PC Windows (3 étapes simples — aucune installation requise)

1. **Extrayez (décompressez)** le fichier `.zip` téléchargé (`Clic droit -> Extraire tout...`), ouvrez le dossier **`pc-web`** et double-cliquez sur **`Start_CERAMICA_PC.bat`** (Mac/Linux : `./start_pc.sh`).
   - Le serveur local démarre automatiquement sur `http://localhost:8173` puis ouvre **Google Chrome**, **Edge (récent)** ou **Firefox**.
   - *(Gardez la petite fenêtre noire ouverte pendant que vous utilisez CERAMICA POS. N'ouvrez pas `index.html` directement en `file://`.)*
2. **Configurer Firebase Web (une seule fois sur le PC)** :
   - Cliquez sur le lien **Ouvrir Firebase Console** affiché à l'écran.
   - Dans **Paramètres du projet -> Vos applications**, cliquez sur **`</>` (Web)**, donnez un nom (ex: `CERAMICA PC`) puis copiez le bloc `const firebaseConfig = { ... }`.
   - Collez-le dans la fenêtre CERAMICA PC et cliquez sur **Enregistrer & Connecter Google**.
3. **Se connecter avec le même compte Google que sur Android** :
   - Votre stock, factures, clients, fournisseurs, caisse, équipe et alertes dépôt apparaissent et se mettent à jour en direct dans les deux sens (PC ⇄ Android).
