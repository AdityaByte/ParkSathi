"""This file mainly handles all the routes of the v1 api"""
from http import HTTPStatus

from fastapi import APIRouter, Depends, UploadFile, Response
from fastapi.params import Form

from app.api.endpoints.owner import create_owner
from app.auth.firebase import verify_firebase_token, get_current_user
from app.api.endpoints.user import create_user, verify_user_role
from app.schema.parking import ParkingResponse
from app.model.user import User

router = APIRouter()

# Block start - auth routes mapping.

@router.get("/auth/verify-role")
async def handle_verify_role(response: Response, token: dict = Depends(verify_firebase_token)):
    return await verify_user_role(token)

# Block end - auth routes mapping.



# Block start - user routes mapping.

@router.post("/user/create")
async def handle_create_user(response: Response, decoded_token: dict = Depends(verify_firebase_token)):
    # Once the user signs up with Google at the client side we get the decoded token.
    # From this token, we need to create a user.
    return await create_user(response, decoded_token)

# Block end - user routes mapping.


# Block start - owner routes mapping.

@router.post("/owner/create", response_model=ParkingResponse, status_code=HTTPStatus.CREATED)
async def handle_create_owner(
        parking_name: str = Form(...),
        address: str = Form(...),
        phone_number: str = Form(...),
        lat: float = Form(...),
        lng: float = Form(...),
        slots: int = Form(...),
        id_proof: str = Form(...),
        file: UploadFile = Form(...),
        current_user: User =  Depends(get_current_user)
):
    """This function initially creates the owner as temporary, and thereafter permanent validation,
    we will change the parking owner status as approved."""
    return await create_owner(parking_name, address, phone_number, lat, lng, id_proof, slots, file, current_user)

# Block end - owner routes mapping.



# Block start - health route mapping.

@router.get("/health")
async def handle_health():
    return {"message": "OK"}

# Block end - health route mapping.
