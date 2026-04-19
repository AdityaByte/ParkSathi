"""This file has mainly the service logic for the payment endpoint."""

from datetime import datetime
from http import HTTPStatus
from fastapi import HTTPException
from app.model.booking import Booking, BookingStatus
from app.model.parking import ParkingDetails
from app.model.user import User
from app.schema.payment import PaymentResponse
from uuid import UUID

# Now we are moved from this static thing to dynamic - (as of i have used this for simplicity.)
# PAYMENT_AMOUNT_PER_MINUTE: float = 10.0 # In ruppees. Just for as a dummy amount.

payment_map: dict[str, PaymentResponse] = {}


async def create_payment(uid: str, booking_id: str) -> PaymentResponse:
    # Firstly we need to check that with the booking id any booking is existed or not.
    booking = await Booking.find_one(Booking.booking_id == UUID(booking_id))
    if booking is None:
        raise HTTPException(status_code=HTTPStatus.BAD_REQUEST, detail="No booking existed with the booking id.")
        
    # For second step we need to check that is this booking is acquired or not if not then we just say the booking is not acquired.
    if booking.booking_status != BookingStatus.ACQUIRED:
        raise HTTPException(status_code=HTTPStatus.BAD_REQUEST, detail="Booking is not acquired right now, try to acquire it first then try again later.")
        
    # If the booking passes these checks then we need to calcute the time.
    acquired_time = booking.acquired_at
    if not acquired_time:
        raise HTTPException(status_code=HTTPStatus.INTERNAL_SERVER_ERROR, detail="acquired time is null.")
    
    time_difference = datetime.now() - acquired_time
    time_difference_in_minutes = time_difference.total_seconds() / 60
    
    parking_details = await ParkingDetails.find_one(ParkingDetails.parking_id == booking.parking_id)
    
    amount = round(time_difference_in_minutes * (parking_details.hourly_rate / 60.0))
    
    user_details = await User.find_one(User.uid == parking_details.uid)
    
    # Now I just need to create the DTO and return it.
    response = PaymentResponse(
        owner_name=user_details.name,
        booking_id=booking.booking_id,
        parking_id=booking.parking_id,
        time_stamp=time_difference_in_minutes,
        amount=amount
    )
    
    payment_map[str(booking.booking_id)] = response
    
    return response
    
async def make_payment(booking_id: str):
    response = payment_map[booking_id]
    if response is None:
        raise HTTPException(status_code=HTTPStatus.BAD_REQUEST, detail="No payment response is created earlier try to create payment first.")
    
    # If we gets the response then we just need to make the payment.
    booking = await Booking.find_one(Booking.booking_id == UUID(booking_id))
    if booking is not None:
        parking_details = await ParkingDetails.find_one(ParkingDetails.parking_id == booking.parking_id)    
        # Then we just mark the booking status as completed at the end before that we need to add the amount to the owner.
        parking_details.total_income = parking_details.total_income + response.amount
        parking_details.acquired_slots -= 1
        await parking_details.save()
        
        # Now we need to change the booking status.
        booking.completed_at = datetime.now()
        booking.booking_status = BookingStatus.COMPLETED
        await booking.save()
        
        return {"status": "DONE", "message": "Payment successful."}
    