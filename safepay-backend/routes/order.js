const express = require("express");
const router = express.Router();
const Order = require("../models/Order");
const verifyVideo = require("../utils/verify");

// 📦 POST /api/orders → Place Order
router.post("/", async (req, res) => {
    const { product, customer } = req.body;

    // Basic validation
    if (!product || !customer) {
        return res.status(400).json({ message: "Missing product or customer info." });
    }

    if (!customer.name || !customer.phoneNumber || !customer.address) {
        return res.status(400).json({ message: "Incomplete customer details." });
    }

    try {
        const newOrder = new Order({
            product: {
                name: product.name,
                description: product.description,
                price: product.price,
                images: product.images
            },
            customer: {
                name: customer.name,
                phoneNumber: customer.phoneNumber,
                address: customer.address
            },
            status: "escrow",
            verificationStatus: "not_checked"
        });

        const savedOrder = await newOrder.save();

        res.status(201).json({
            message: "Order placed successfully.",
            orderId: savedOrder._id
        });
    } catch (err) {
        console.error("❌ Error creating order:", err);
        res.status(500).json({ message: "Server error." });
    }
});

// 📄 GET /api/orders/:phoneNumber → Get orders for a customer
router.get("/:phoneNumber", async (req, res) => {
    const { phoneNumber } = req.params;

    try {
        const orders = await Order.find({ "customer.phoneNumber": phoneNumber }).sort({ createdAt: -1 });

        if (orders.length === 0) {
            return res.status(404).json({ message: "No orders found for this phone number." });
        }

        res.status(200).json({ orders });
    } catch (err) {
        console.error("❌ Error fetching orders:", err);
        res.status(500).json({ message: "Server error." });
    }
});

// 🔧 POST /api/orders/verify
router.post("/verify", async (req, res) => {
    const { orderId, videoUrl } = req.body;

    if (!orderId || !videoUrl) {
        return res.status(400).json({ message: "orderId and videoUrl are required." });
    }

    try {
        const order = await Order.findById(orderId);
        if (!order) return res.status(404).json({ message: "Order not found" });

        const imageUrls = order.product.images;

        if (!imageUrls || imageUrls.length === 0) {
            return res.status(400).json({ message: "No product images found for this order." });
        }

        // Run verification
        const threshold = 0.6;
        const result = await verifyVideo(videoUrl, imageUrls, threshold);
        const matchScore = result.bestScore || 0;

        // Decide outcome
        let verificationStatus = "fail";
        let status = "refunded";
        let payoutReleased = false;
        let refundIssued = true;

        if (result.match) {
            verificationStatus = "pass";
            status = "released";
            payoutReleased = true;
            refundIssued = false;
        }

        // Update order
        order.videoUrl = videoUrl;
        order.verificationStatus = verificationStatus;
        order.status = status;
        order.payoutReleased = payoutReleased;
        order.refundIssued = refundIssued;
        await order.save();

        res.status(200).json({
            message: `Verification ${verificationStatus.toUpperCase()}`,
            matchScore: matchScore + "%",
            order
        });

    } catch (err) {
        console.error("❌ Verification error:", err);
        res.status(500).json({ message: "Server error during verification." });
    }
});


module.exports = router;
