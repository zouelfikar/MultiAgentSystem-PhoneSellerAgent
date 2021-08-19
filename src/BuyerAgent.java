
// Package
///////////////
package sellPhones;

// Imports
///////////////

import jade.core.Agent;
import jade.core.AID;

import jade.domain.FIPAException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;

public class BuyerAgent extends Agent {

   protected int pickPhone = (int) (Math.random() * 2);// arrivals
   protected int budget = ((int) (Math.random() * (100 - 30))) + 30;// arrivals
   protected boolean hasPhone = false;

   // Constructors
   //////////////////////////////////

   // External signature methods
   //////////////////////////////////

   /**
    * Set up the agent. Register with the DF, and add a behaviour to process
    * incoming messages. Also sends a message to the host to say that this guest
    * has arrived.
    */
   protected void setup() {
       try {

           // create the agent descrption of itself
           ServiceDescription sd = new ServiceDescription();
           sd.setType("PartyGuest");
           sd.setName("GuestServiceDescription");
           DFAgentDescription dfd = new DFAgentDescription();
           dfd.setName(getAID());
           dfd.addServices(sd);
           System.out.println("Agent  " + getLocalName() + " has " + budget + "$");
           // register the description with the DF
           DFService.register(this, dfd);

           // notify the host that we have arrived
           ACLMessage hello = new ACLMessage(ACLMessage.INFORM);
           hello.setContent(SellerAgent.HELLO);
           hello.addReceiver(new AID("host", AID.ISLOCALNAME));
           send(hello);
           //
           // add a Behaviour to process incoming messages
           addBehaviour(new CyclicBehaviour(this) {

               public void action() {
                   // listen if a greetings message arrives
                   ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                   if (msg != null) {

                       if (msg.getContent().startsWith(SellerAgent.Iphone)
                               || (msg.getContent().startsWith(SellerAgent.Huawei))
                               || (msg.getContent().startsWith(SellerAgent.Nokia))) {
                           // check if seller doesn't have phone to ask for pricies
                           if (!hasPhone) {
                               askForPrice(msg.getContent());
                           }
                       } else if (msg.getContent().startsWith("0")) {
                           // check if I have money
                           checkmoney(msg.getContent());
                       } else if (msg.getContent().startsWith("Here")) {
                           // I got the phone, send awesome
                           awesome();
                       } else if (msg.getContent().startsWith("Sorry")) {
                           // sorry I don't have enough money for any phone, send OK
                           System.err.println("Sorry, Agent " + getLocalName() + " has only " + budget + "$");
                           ok();
                       } else if (msg.getContent().startsWith(("GOODBYE"))) {
                           // I'm getting out
                           leaveParty();
                       } else {

                           System.out.println("buyer received unexpected message: " + msg);
                       }
                   } else {
                       block();
                   }
               }
           });
       } catch (Exception e) {
           System.out.println("Saw exception in BuyerAgent: " + e);
           e.printStackTrace();
       }

   }

   protected void leaveParty() {
       try {
           DFService.deregister(this);
           doDelete();
       } catch (FIPAException e) {
           System.err.println("Saw FIPAException while leaving party: " + e);
           e.printStackTrace();
       }
   }

   protected void awesome() {

       hasPhone = true;
       ACLMessage m = new ACLMessage(ACLMessage.INFORM);
       m.setContent("Awesome");
       m.addReceiver(new AID("host", AID.ISLOCALNAME));
       send(m);

   }

   protected void ok() {

       hasPhone = true;
       ACLMessage m = new ACLMessage(ACLMessage.INFORM);
       m.setContent("OK");
       m.addReceiver(new AID("host", AID.ISLOCALNAME));
       send(m);

       // leaveParty();
   }

   protected void askForPrice(String phone) {
       ACLMessage m = new ACLMessage(ACLMessage.INFORM);
       m.setContent(phone);
       m.addReceiver(new AID("host", AID.ISLOCALNAME));
       send(m);
   }

   protected void checkmoney(String price) {
       String tempPrice = price.substring(0, 3);
       String tempPhone = price.substring(3);
       int IntPrice = Integer.parseInt(tempPrice);
       // System.out.println( "Agent still wants " + getLocalName());
       if (IntPrice <= budget) {
           ACLMessage buyIt = new ACLMessage(ACLMessage.INFORM);
           buyIt.setContent("DEAL" + tempPhone);
           buyIt.addReceiver(new AID("host", AID.ISLOCALNAME));
           send(buyIt);
       } else {
           ACLMessage NoMoney = new ACLMessage(ACLMessage.INFORM);
           NoMoney.setContent("NoMoney" + tempPhone);
           NoMoney.addReceiver(new AID("host", AID.ISLOCALNAME));
           send(NoMoney);
       }

   }

}
