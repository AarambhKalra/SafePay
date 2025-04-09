const express = require("express");
const router = express.Router();
const Order = require("../models/Order");

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
            status: "pending",
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


module.exports = router;
