const admin = require("../config/firebase");

const verifyFirebaseToken = async (req, res, next) => {
    const idToken = req.headers.authorization?.split('Bearer ')[1];

    if (!idToken) {
        return res.status(401).json({ message: "No token provided" });
    }

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);

        // You now have access to uid, phone_number, etc.
        req.user = {
            uid: decodedToken.uid,
            phone_number: decodedToken.phone_number,
        };
        next();
    } catch (err) {
        console.error("Firebase token verification failed:", err);
        return res.status(401).json({ message: "Invalid token" });
    }
};

module.exports = verifyFirebaseToken;
