# MultiAgentSystem-PhoneSellerAgent

Agent representing the seller agent, to which a user-controlled number of buyers is invited. The sequence is as follows: the user selects a number buyers to attend the party from 0 to 300, using the slider on the UI.
When the Purchas starts, the host creates N buyers agents, each of which registers with the DF,has random budget between 30-100$ and sends the host a message to say that they have arrived.

Seller Agent has three kind of phones (Iphone with 90$, Huawie with 75$, Nokia with 45$).
Once Seller get a message that a buyer arrived, Seller starts the purchas deal and show buyer an Iphone first which the most expensive one.
When Buyer get any type of phones he asked the seller for the phone's price.
When Seller is asked for a specific phone's price, he send that price to buyer.
When buyer get the price he is checking his money:
1. If buyer has money then he will take the phone by sending "Deal" with the phone' names.
		When Seller get the Deal messege he's collecting money and sending the phone to buyer.
		When buyer get the phone he reply with awesome.
		When Seller get awesome he replay with Goodbye which tell buyer to leave

2. if buyer has no enough money for that phone, buyer send "No money"		
		When Seller get "No money" messege, Seller send another phone deal.
		If Seller has no other phones, then send "Sorry".
		When buyer get "Sorry", Buyer Send "OK".
	When Seller get "OK" he replay with Goodbye which tell buyer to leave



  Note: to start the host agent, it must be named 'host'. Thus:
      java jade.Boot -gui host:sellPhones.SellerAgent()
