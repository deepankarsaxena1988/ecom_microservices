const express = require('express');
const app = express();
app.use(express.json());
const PORT = process.env.PORT || 3000;

// POST /checkout - sample endpoint
app.post('/checkout', (req, res) => {
  const { cart, user } = req.body;
  if (!cart || !Array.isArray(cart) || cart.length === 0) {
    return res.status(400).json({ error: 'Invalid cart' });
  }

  // Simulate order creation (replace with real logic)
  const order = {
    id: Date.now(),
    user: user || null,
    items: cart,
    status: 'created'
  };

  return res.status(201).json({ order });
});

app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.listen(PORT, () => console.log(`ecomCheckOutService listening on ${PORT}`));
