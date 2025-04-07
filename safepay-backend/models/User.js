const mongoose = require("mongoose");

const userSchema = new mongoose.Schema(
    {
        firebaseUid: {
            type: String,
            required: true,
            unique: true,
        },
        phoneNumber: {
            type: String,
            required: true,
        },
        role: {
            type: String,
            enum: ["customer", "vendor", "admin"],
            default: "customer",
        },

        // Vendor-specific fields (RazorpayX)
        storeName: String,
        razorpayXContactId: String,
        razorpayXFundAccountId: String,
        bankAccountVerified: {
            type: Boolean,
            default: false,
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model("User", userSchema);
