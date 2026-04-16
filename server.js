const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const app = express();

app.use(cors(), express.json());

// Remplace par ton lien MongoDB Atlas si nécessaire
const mongoURI = "mongodb+srv://juniornatolo_db_user:2mdpn3Lwow5rhOss@cluster0.c6gsh8q.mongodb.net/eglise_db?retryWrites=true&w=majority";

mongoose.connect(mongoURI)
  .then(() => console.log("⛪ SYSTÈME ÉGLISE : CONNECTÉ AU CLOUD"))
  .catch(err => console.log("❌ Erreur de connexion:", err));

// MODÈLES DE DONNÉES
const Membre = mongoose.model('Membre', { 
    nom: String, 
    telephone: String, 
    ministere: String, 
    dateAdhesion: { type: Date, default: Date.now } 
});

const Finance = mongoose.model('Finance', { 
    type: String, // Dîme, Offrande, Don
    montant: Number, 
    donateur: String,
    date: { type: Date, default: Date.now } 
});

// ROUTES API
app.post('/membres/ajouter', async (req, res) => {
    const m = new Membre(req.body);
    await m.save();
    res.send({ message: "Membre enregistré !" });
});

app.get('/membres/liste', async (req, res) => {
    const membres = await Membre.find();
    res.json(membres);
});

app.post('/finances/enregistrer', async (req, res) => {
    const f = new Finance(req.body);
    await f.save();
    res.send({ message: "Transaction enregistrée !" });
});

app.listen(3001, () => console.log("🚀 LOGICIEL ÉGLISE PRÊT SUR LE PORT 3001"));
