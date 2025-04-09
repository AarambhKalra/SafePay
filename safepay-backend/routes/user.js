const express = require('express');
const router = express.Router();
const verifyFirebaseToken = require('../middleware/auth');
const User = require('../models/User');

// Get current user info
router.get('/me', verifyFirebaseToken, async (req, res) => {
    const { uid, phone_number } = req.user;
    // console.log(uid, phone_number);

    let user = await User.findOne({ firebaseUid: uid });
    if (!user) {
        user = await User.create({ firebaseUid: uid, phoneNumber: phone_number });
        // console.log(user);
    }

    res.json({ user });
});

module.exports = router;
