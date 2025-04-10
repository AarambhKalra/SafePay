const mongoose = require("mongoose");

const orderSchema = new mongoose.Schema(
    {
        // Product Info
        product: {
            name: { type: String, required: true },
            description: String,
            price: { type: Number, required: true },
            images: [String]
        },

        // Customer Info
        customer: {
            name: { type: String, required: true },
            phoneNumber: { type: String, required: true },
            address: { type: String, required: true }
        },

        // Verification
        videoUrl: { type: String },
        verificationStatus: {
            type: String,
            enum: ["not_checked", "pass", "fail"],
            default: "not_checked"
        },

        // Order Status
        status: {
            type: String,
            enum: ["released", "refunded", "escrow"],
            default: "escrow"
        },

        // Internal flags
        payoutReleased: { type: Boolean, default: false },
        refundIssued: { type: Boolean, default: false }
    },
    { timestamps: true }
);

module.exports = mongoose.model("Order", orderSchema);
