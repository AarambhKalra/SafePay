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

// 🆕 Vendor Onboarding Route
router.post("/onboard-vendor", verifyFirebaseToken, async (req, res) => {
    const { storeName } = req.body;

    if (!storeName) {
        return res.status(400).json({ message: "storeName is required" });
    }

    try {
        let user = await User.findOne({ firebaseUid: req.user.uid });

        if (!user) {
            // First-time vendor, create user
            user = new User({
                firebaseUid: req.user.uid,
                phoneNumber: req.user.phone_number,
                role: "vendor",
                storeName
            });
        } else {
            // Update existing user to vendor
            user.role = "vendor";
            user.storeName = storeName;
        }

        await user.save();

        res.json({ message: "Vendor onboarded", user });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error" });
    }
});

module.exports = router;
