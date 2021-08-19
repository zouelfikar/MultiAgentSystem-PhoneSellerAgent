
// Package
///////////////
package sellPhones;

// Imports
///////////////
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Profile;

import jade.wrapper.PlatformController;
import jade.wrapper.AgentController;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import javax.swing.*;

import sellPhones.HostUIFrame;

import java.util.*;
import java.text.NumberFormat;

/**
* <p>
Phone Seller Agent:

Agent representing the seller agent, to which a user-controlled number 
of buyers is invited. The sequence is as follows: the user selects a 
number buyers to attend the party from 0 to 300, using the slider
on the UI.
When the Purchas starts, the host creates N buyers agents, each of 
which registers with the DF, and sends the host a message to say that
they have arrived.

Seller Agent has three kind of phones (Iphone with 90$, Huawie with 75$,
Nokia with 45$).
Once Seller get a message that a buyer arrived, Seller starts the purchas
deal and show buyer an Iphone first which the most expensive one.
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

* <p>
* Note: to start the host agent, it must be named 'host'. Thus: <code><pre>
*     java jade.Boot -gui host:sellPhones.SellerAgent()
* </pre></code>
* </p>
*

*/
public class SellerAgent extends Agent {
   // Constants
   //////////////////////////////////

   public final static String HELLO = "HELLO";
   public final static String Iphone = "IPHONE";
   public final static String Huawei = "Huawei";
   public final static String Nokia = "Nokia";
   public final static String GOODBYE = "GOODBYE";
   public final static String[] Pricies = { "090", "075", "045" };


   // Instance variables
   //////////////////////////////////
   protected JFrame m_frame = null;
   protected Vector m_sellersList = new Vector();
   protected int m_sellerCount = 0;
   protected int numOfCheckedAgent = 0;
   protected int Total = 0;
   protected int numOfAgentWhoDoesntBuy = 0;
   protected int m_numOfSoledPhones = 0;
   protected boolean m_purchaseOver = false;
   protected NumberFormat m_avgFormat = NumberFormat.getInstance();
   protected long m_startTime = 0L;
   protected long m_endTime = 0L;
   protected long m_Time = 0L;

   // Constructors
   //////////////////////////////////

   /**
    * Construct the host agent. Some tweaking of the UI parameters.
    */
   public SellerAgent() {
       m_avgFormat.setMaximumFractionDigits(2);
       m_avgFormat.setMinimumFractionDigits(2);
   }

   // External signature methods
   //////////////////////////////////

   /**
    * Setup the agent. Registers with the DF, and adds a behaviour to process
    * incoming messages.
    */
   protected void setup() {

       try {
           System.out.println("Seller Agent is ready for his deals ");
           DFAgentDescription dfd = new DFAgentDescription();
           dfd.setName(getAID());
           DFService.register(this, dfd);

           // add the GUI
           setupUI();
           //
           //
           addBehaviour(new CyclicBehaviour(this) {
               public void action() {
                   ACLMessage msg = receive();

                   if (msg != null) {
                       if (HELLO.equals(msg.getContent())) {
                           // meet new seller and start the Deal 
                           m_sellerCount++;
                           setStatus("metting new Buyer Agent (" + m_sellerCount + ")");
                           beginDeal(msg.getSender());
                           //check if Seller meet all expected buyer Agents
                           if (m_sellerCount == m_sellersList.size()) {
                               System.out.println("All expected Agent has been met");
                           }
                       } else if (msg.getContent().startsWith(Iphone) || (msg.getContent().startsWith(Huawei))
                               || (msg.getContent().startsWith(Nokia))) {
                           // send phone's price to the seller
                           sendPrice(msg.getContent(), msg.getSender());
                       } else if (msg.getContent().startsWith("DEAL")) {
                           // buyer bought the phone, take money and send the phone to buyer
                           deal(msg.getContent(), msg.getSender());
                       } else if (msg.getContent().startsWith(("NoMoney"))) {
                           // buyer has no money send him another phone to check
                           sendAnotherPhone(msg.getContent(), msg.getSender());
                       } else if (msg.getContent().startsWith(("Awesome")) || msg.getContent().startsWith(("OK"))) {
                           // Buyer got his new phone or he has no enough money, then check if we checked all agents
                           numOfCheckedAgent = numOfAgentWhoDoesntBuy + m_numOfSoledPhones;
                           if (m_sellersList.size() == numOfCheckedAgent) {
                               endParty();
                               System.out.println("================================================================");
                           }

                       } else {
                           System.out.println("Seller received unexpected message: " + msg);
                       }
                   } else {
                       block();
                   }
               }
           });
       } catch (Exception e) {
           System.out.println("Saw exception in SellerAgent: " + e);
           e.printStackTrace();
       }

   }

   protected void sendAnotherPhone(String phone, AID guest0) {
       int price = 0;
       String NewPhone = "";

       String tempPhone = phone.substring(7);
       switch (tempPhone) {
           case Iphone:
               NewPhone = Huawei;
               break;

           case Huawei:
               NewPhone = Nokia;
               break;

           case Nokia:
               NewPhone = "Sorry";
               break;
       }
       if (NewPhone == "Sorry") {
           numOfAgentWhoDoesntBuy++;
           SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   ((HostUIFrame) m_frame).numOfPoorAgents.setText(Integer.toString(numOfAgentWhoDoesntBuy));
               }
           });

       }

       ACLMessage finsied = new ACLMessage(ACLMessage.INFORM);
       finsied.setContent(NewPhone);
       finsied.addReceiver(guest0);
       send(finsied);
   }

   protected void deal(String phone, AID guest0) {
       int price = 0;

       String tempPhone = phone.substring(4);
       switch (tempPhone) {
           case Iphone:
               price = Integer.parseInt(Pricies[0]);
               break;

           case Huawei:
               price = Integer.parseInt(Pricies[1]);
               break;

           case Nokia:
               price = Integer.parseInt(Pricies[2]);
               break;
       }
       Total += price;

       System.out.println(guest0.getName() + " has bought : " + tempPhone + " at price of " + price);
       m_numOfSoledPhones++;
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               ((HostUIFrame) m_frame).lbl_numISoldPhones.setText(Integer.toString(m_numOfSoledPhones));
           }
       });
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               ((HostUIFrame) m_frame).lbl_total.setText(Integer.toString(Total) + "$");
           }
       });
       ACLMessage finsied = new ACLMessage(ACLMessage.INFORM);
       finsied.setContent("Here is your new Phone");
       finsied.addReceiver(guest0);
       send(finsied);
   }

   protected void sendPrice(String phone, AID guest0) {
       String price = "";
       switch (phone) {
           case Iphone:
               price = Pricies[0] + phone;
               break;

           case Huawei:
               price = Pricies[1] + phone;
               break;

           case Nokia:
               price = Pricies[2] + phone;
               break;
       }
       ACLMessage priceValue = new ACLMessage(ACLMessage.INFORM);
       priceValue.setContent(price);
       priceValue.addReceiver(guest0);
       send(priceValue);
   }

   protected void beginDeal(AID guest0) {
       ACLMessage firstphone = new ACLMessage(ACLMessage.INFORM);
       firstphone.setContent(Iphone);
       firstphone.addReceiver(guest0);
       send(firstphone);

       setStatus("purchasee conversation");
   }

   protected void setStatus(final String state) {
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               ((HostUIFrame) m_frame).lbl_dealState.setText(state);
           }
       });
   }

   // Internal implementation methods
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Setup the UI, which means creating and showing the main frame.
    */
   private void setupUI() {
       m_frame = new HostUIFrame(this);

       m_frame.setSize(400, 200);
       m_frame.setLocation(400, 400);
       m_frame.setVisible(true);
       m_frame.validate();
   }

   /**
    * Invite a number of buyers, as determined by the given parameter. Clears old
    * state variables, then creates N buyers agents. A list of the agents is
    * maintained, so that the host can tell them all to leave at the end of the
    * deal.
    *
    * @param nGuests The number of buyer agents to invite.
    */
   protected void inviteGuests(int nGuests) {
       // remove any old state
       m_sellersList.clear();
       m_sellerCount = 0;
       m_numOfSoledPhones = 0;
       Total = 0;
		  m_Time = 0;
		  numOfAgentWhoDoesntBuy = 0;
       m_purchaseOver = false;
       ((HostUIFrame) m_frame).lbl_numISoldPhones.setText("0");

       // notice the start time
       m_startTime = System.currentTimeMillis();

       setStatus("Inviting guests");

       PlatformController container = getContainerController(); // get a container controller for creating new agents
       // create N guest agents
       try {
           for (int i = 0; i < nGuests; i++) {
               // create a new agent
               String localName = "guest_" + i;
               AgentController guest = container.createNewAgent(localName, "sellPhones.BuyerAgent", null);
               guest.start();

               // keep the guest's ID on a local list
               m_sellersList.add(new AID(localName, AID.ISLOCALNAME));
           }
       } catch (Exception e) {
           System.err.println("Exception while adding guests: " + e);
           e.printStackTrace();
       }
   }

   /**
    * End the party: set the state variables, and tell all the guests to leave.
    */
   protected void endParty() {
       setStatus("Party over");
       m_purchaseOver = true;

       // send a message to all guests to tell them to leave
       for (Iterator i = m_sellersList.iterator(); i.hasNext();) {
           ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
           msg.setContent(GOODBYE);

           msg.addReceiver((AID) i.next());

           send(msg);
       }

       m_sellersList.clear();
       m_endTime = System.currentTimeMillis();
       m_Time = m_endTime - m_startTime;
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               ((HostUIFrame) m_frame).lbl_duration.setText(Long.toString(m_Time) + " ms");
           }
       });
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               ((HostUIFrame) m_frame).enableControls(false);
           }
       });

   }

   protected void terminateHost() {
       try {
           if (!m_sellersList.isEmpty()) {
               endParty();
           }

           DFService.deregister(this);
           m_frame.dispose();
           doDelete();
       } catch (Exception e) {
           System.err.println("Saw FIPAException while terminating: " + e);
           e.printStackTrace();
       }
   }

}
