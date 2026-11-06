-- Задание 3: клиенты из города London
SELECT CustomerID, FirstName, LastName
FROM Customers
WHERE City = 'London';

-- Задание 4: общая сумма заказов по каждому клиенту, сортировка по убыванию
SELECT CustomerID, SUM(Amount) AS TotalAmount
FROM Orders
GROUP BY CustomerID
ORDER BY TotalAmount DESC;

-- Задание 5: имена/фамилии и количество заказов, только клиенты с более чем 2 заказами
SELECT c.FirstName, c.LastName, COUNT(o.OrderID) AS TotalOrders
FROM Customers c
JOIN Orders o ON c.CustomerID = o.CustomerID
GROUP BY c.CustomerID, c.FirstName, c.LastName
HAVING COUNT(o.OrderID) > 2
ORDER BY TotalOrders DESC;
