const express = require('express');
const cors = require('cors');

const userRoutes = require('./routes/user');

const app = express();

app.use(cors());
app.use(express.json());

app.use('/api/users', userRoutes);

module.exports = app;
