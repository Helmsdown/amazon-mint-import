# amazon-mint-import

If you use [Mint](https://mint.com) and [Amazon](https://amazon.com) and ever tried to categorize your Amazon
purchases (for budgeting purposes) you probably ran into the problem that a given Amazon purchase could span multiple
categories. 

This is a MVP to take [Amazon Order Report CSV files](https://www.amazon.com/gp/b2b/reports?ref_=ya_d_l_order_reports&) 
and "import" them into Mint via Selenium "screen-scraping". A Mint transaction per CSV row is created. The transaction is tagged with
a custom tag (that must be created before hand), given appropriate notes, and it put in the "uncategorized" category.
Care is taken not to import the same transaction twice.

It is then up to you to go through you imported Amazon purchases and correctly categorize them.

Additionally, you must take the follow steps to not double count Amazon purchases in Mint (with respect to Mint Budgets & Trends).

1. Create a new Tag called "Amazon (Per-Item)" (mentioned above)
2. Create a transaction rule to rename Amazon transactions to Amazon.com and categorize them as "Uncategorized".
2. Create a new Tag called "Amazon.com"
3. Exclude "Amazon.com" Tag transactions from Budgets and Trends
4. As new Amazon.com purchases are picked up by Mint. Tag them with Amazon.com  

Finally,
This code assume you have two factor authentication turned on for your Intuit/Mint account. It is smart enough to handle 
Authenticator and Text Message Code 2FA requests.

## Disclaimer

The author takes no responsibility for your use of this code. Use it at your own risk. The code is open source and you can
 see exactly what it does. Given the sensitive nature of your Intuit/Mint account and Amazon purchase history the author recommends
 people who would use this code to read it thoroughly and understand how it works.


## Mint Engineers and PMs

If you happen to work on Mint or know someone who does. Please refer them to this repo so they can see
how I am overcoming this feature gap. I would **love** to buy a a Mint engineer or PM lunch and talk about the product
and where I think it could be improved. 
