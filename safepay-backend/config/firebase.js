const admin = require("firebase-admin");

// const serviceAccount = require("../firebase-service-account.json");
const serviceAccount = JSON.parse(process.env.FIREBASE_CONFIG);

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});

module.exports = admin;
