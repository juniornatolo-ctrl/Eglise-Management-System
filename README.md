# ⛪ Église Management System

Solution desktop complète développée en **Java (Swing)** pour la gestion moderne des paroisses. L'application permet de gérer les membres, suivre les contributions financières, planifier les événements et sacrements, communiquer avec la communauté et générer des rapports avec graphiques intégrés.

Un module additionnel en **Node.js / Express / MongoDB** permet une synchronisation cloud optionnelle des données (API REST).

## ✨ Fonctionnalités

- **Gestion des membres** — fiches, coordonnées, ministères, date d'adhésion
- **Finances** — suivi des dîmes, offrandes et dons (`FinancePanel`)
- **Événements** — planification et suivi (`EvenementPanel`)
- **Présence** — suivi des présences aux cultes et activités (`PresencePanel`)
- **Sacrements** — gestion des baptêmes, mariages, etc. (`SacrementPanel`)
- **Communication** — messagerie interne à la communauté (`CommunicationPanel`, `CommunicationService`)
- **Rapports** — tableau de bord et graphiques (`DashboardPanel`, `ReportService`)
- **Sauvegarde automatique** — export/restauration des données (`BackupPanel`, `BackupService`)
- **Authentification** — connexion sécurisée (`LoginDialog`, `AuthService`)
- **Paramètres** — configuration de l'application (`SettingsPanel`)

## 🏗️ Architecture

```
src/com/eglise/
├── db/           # Gestion de la base de données locale
├── model/        # Entités (Membre, Transaction, Evenement, Sacrement, Presence, Message)
├── service/      # Logique métier (Auth, Backup, Communication, Report)
└── ui/           # Interface graphique Swing (panels + fenêtre principale)
```

- **Stockage local** : base de données embarquée dans `~/EgliseApp/` (fichiers de données par table)
- **API cloud (optionnelle)** : `server.js` — API Express connectée à MongoDB Atlas pour synchroniser membres et finances

## 🛠️ Prérequis

- Java JDK 8 ou supérieur
- (Optionnel, pour le module API) Node.js 18+ et npm

## 🚀 Installation et lancement

### Application desktop (Java)

```bash
git clone https://github.com/juniornatolo-ctrl/Eglise-Management-System.git
cd Eglise-Management-System
chmod +x build.sh
./build.sh
java -jar EgliseManager.jar
```

Le script `build.sh` compile les sources dans `bin/` puis génère `EgliseManager.jar`.

### API cloud (optionnelle)

```bash
npm install express mongoose cors
```

Créer un fichier `.env` à la racine (ne pas le versionner) :

```
MONGO_URI=mongodb+srv://<utilisateur>:<mot_de_passe>@<cluster>/eglise_db?retryWrites=true&w=majority
PORT=3001
```

Puis lancer :

```bash
node server.js
```

L'API démarre sur `http://localhost:3001` avec les routes :

| Méthode | Route | Description |
|---|---|---|
| POST | `/membres/ajouter` | Ajouter un membre |
| GET | `/membres/liste` | Lister les membres |
| POST | `/finances/enregistrer` | Enregistrer une transaction financière |

## 🔒 Sécurité

- Ne jamais committer d'identifiants ou de chaînes de connexion en clair dans le code source.
- Utiliser des variables d'environnement (`.env`, ajouté au `.gitignore`) pour toute information sensible (base de données, clés API).
- En cas d'exposition accidentelle d'un identifiant, le régénérer immédiatement côté fournisseur (ex. MongoDB Atlas).

## 📄 Licence

Projet personnel — tous droits réservés, sauf mention contraire.

## 👤 Auteur

**NATOLO Junior** — Développeur freelance & Data Scientist et Analyst
[@JUNIORNATOLO](https://github.com/juniornatolo-ctrl)
