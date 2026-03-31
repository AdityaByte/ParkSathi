from pydantic import BaseModel
from uuid import UUID

# For this instance we are not making it so much complex we are just storing the parking data as in a dictionary.
# later on we will moved to a seperate collection in db.

class PaymentResponse(BaseModel):
    # We are skipping the UID in this as we get the UID directly by the token.
    owner_name: str
    booking_id: UUID
    parking_id: UUID
    time_stamp: float # This mainly shows for how much time the vehicle is parked.
    amount: float