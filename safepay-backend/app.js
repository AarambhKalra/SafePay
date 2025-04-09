const express = require('express');
const cors = require('cors');
const orderRoutes = require("./routes/order");

const userRoutes = require('./routes/user');

const app = express();

app.use(cors());
app.use(express.json());

app.use('/api/users', userRoutes);
app.use("/api/orders", orderRoutes);

module.exports = app;
