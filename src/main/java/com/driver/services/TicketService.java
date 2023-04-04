package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        SeatAvailabilityEntryDto seatAvailabilityEntryDto=new SeatAvailabilityEntryDto();
        seatAvailabilityEntryDto.setFromStation(bookTicketEntryDto.getFromStation());
        seatAvailabilityEntryDto.setToStation(bookTicketEntryDto.getToStation());
        seatAvailabilityEntryDto.setTrainId(bookTicketEntryDto.getTrainId());
         int availableSeats=calculateAvailableSeats(seatAvailabilityEntryDto);
         if(availableSeats==0){
             throw new Exception("Invalid stations");
         }
         if(availableSeats<bookTicketEntryDto.getNoOfSeats()){
             throw new Exception("Less tickets are available");
         }
         Ticket ticket=new Ticket();
         ticket.setFromStation(bookTicketEntryDto.getFromStation());
         ticket.setToStation(bookTicketEntryDto.getToStation());
         ticket.setTrain(train);
         List<Passenger>passengers=new ArrayList<>();
         for(int i:bookTicketEntryDto.getPassengerIds()){
             Passenger passenger=passengerRepository.findById(i).get();
             passengers.add(passenger);
         }
        ticket.setPassengersList(passengers);
         Passenger passenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
         List<Ticket>ticketList=passenger.getBookedTickets();
         ticketList.add(ticket);
         passenger.setBookedTickets(ticketList);
        List<Ticket>tickets=train.getBookedTickets();
        tickets.add(ticket);
        train.setBookedTickets(tickets);
         ticketRepository.save(ticket);
         passengerRepository.save(passenger);
         trainRepository.save(train);

       return ticket.getTicketId();

    }
    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket>ticketList=train.getBookedTickets();
        String []trainRoot=train.getRoute().split(",");
        HashMap<String,Integer> map=new HashMap<>();
        for(int i=0;i<trainRoot.length;i++){
            map.put(trainRoot[i],i);
        }
        if(!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString())||!map.containsKey(seatAvailabilityEntryDto.getToStation().toString())){
            return 0;
        }
        int count=train.getNoOfSeats()-ticketList.size();
        for(Ticket t:ticketList){
            String fromStation=t.getFromStation().toString();
            String toStation=t.getToStation().toString();
            if(map.get(seatAvailabilityEntryDto.getToStation().toString())<=map.get(fromStation)){
                count++;
            }
            else if (map.get(seatAvailabilityEntryDto.getFromStation().toString())>=map.get(toStation)){
                count++;
            }
        }
        return count;
    }
}
