"""This file mainly handles all the routes of the v1 api"""

from http import HTTPStatus
import uuid

from fastapi import APIRouter, Depends, Response, UploadFile, WebSocket
from fastapi.params import Form
from firebase_admin.auth import UidIdentifier

from app.api.endpoints.booking import acquire_booking, cancel_booking, create_booking, get_bookings, get_bookings_by_parking_id
from app.api.endpoints.admin import change_owner_form_verification_status, get_pending_owner_verification_form
from app.api.endpoints.owner import create_owner
from app.api.endpoints.parking import find_nearby_parking_spot
from app.api.endpoints.user import create_user, verify_user_role
from app.auth.firebase import get_current_user, verify_firebase_token
from app.model.booking import BookingStatus
from app.model.parking import ParkingDetails, VerificationStatus
from app.model.user import User
from app.schema.booking import BookingResponse
from app.schema.dashboard import DashboardResponse
from app.schema.parking import ParkingResponse
from app.config.websocket_mgr import manager

router = APIRouter()

# Block start - auth routes mapping.


@router.get("/auth/verify-role")
async def handle_verify_role(
    response: Response, token: dict = Depends(verify_firebase_token)
):
    return await verify_user_role(token)


# Block end - auth routes mapping.


# Block start - user routes mapping.


@router.post("/user/create")
async def handle_create_user(
    response: Response, decoded_token: dict = Depends(verify_firebase_token)
):
    # Once the user signs up with Google at the client side we get the decoded token.
    # From this token, we need to create a user.
    return await create_user(response, decoded_token)


# Block end - user routes mapping.


# Block start - owner routes mapping.


@router.post(
    "/owner/create", response_model=ParkingResponse, status_code=HTTPStatus.CREATED
)
async def handle_create_owner(
    parking_name: str = Form(...),
    address: str = Form(...),
    phone_number: str = Form(...),
    lat: float = Form(...),
    lng: float = Form(...),
    slots: int = Form(...),
    id_proof: str = Form(...),
    file: UploadFile = Form(...),
    current_user: User = Depends(get_current_user),
):
    """This function initially creates the owner as temporary, and thereafter permanent validation,
    we will change the parking owner status as approved."""
    return await create_owner(
        parking_name,
        address,
        phone_number,
        lat,
        lng,
        id_proof,
        slots,
        file,
        current_user,
    )


# Block end - owner routes mapping.


# Block start - health route mapping.


@router.get("/health")
async def handle_health():
    return {"message": "OK"}


# Block end - health route mapping.

# Block start - admin route mapping.

@router.get("/admin/pending_owners", status_code=HTTPStatus.OK)
async def handle_get_pending_owners():
    return await get_pending_owner_verification_form()
    
@router.post("/admin/approve", status_code=HTTPStatus.OK)
async def handle_apporve_owner(user_id: str):
    return await change_owner_form_verification_status(user_id, VerificationStatus.APPROVED)
    

# Block end - admin route mapping.


# Block start - parking routes.

@router.post("/parking/nearby", status_code=HTTPStatus.OK)
async def handle_find_nearby_parking(lat: float, lng: float):
    return await find_nearby_parking_spot(lat, lng)


# Block end - parking routes.



# Block start - booking routes.

@router.post("/bookings/create", status_code=HTTPStatus.CREATED)
async def handle_create_booking(parking_id: str, token: str = Depends(verify_firebase_token)):
    return await create_booking(token['uid'], parking_id)
    
@router.get("/bookings/my", status_code=HTTPStatus.OK)
async def handle_get_all_bookings(token: str = Depends(verify_firebase_token)):
    return await get_bookings(token['uid'])
    
@router.post("/bookings/cancel")
async def handle_cancel_booking(booking_id: str, token: str = Depends(verify_firebase_token)):
    return await cancel_booking(booking_id, token['uid'])

@router.post("/bookings/acquire/{booking_id}", status_code=HTTPStatus.OK)
async def handle_acquire_booking(booking_id: str):
    return await acquire_booking(booking_id, BookingStatus.ACQUIRED)
    
@router.get("/bookings/owner")
async def handle_get_bookings_of_owner(token: str = Depends(verify_firebase_token)):
    return await get_bookings_by_parking_id(uid=token['uid'])

# Block end - booking routes.

# Block start - websocket endpoints.
# 
@router.websocket("/ws/partner")
async def partner_socket(websocket: WebSocket, token: str):
    await websocket.accept() 
    uid = None
    try:
        user_token = verify_firebase_token(token)
        uid = user_token['uid']
        
        await manager.connect(websocket, uid)
        
        parking = await ParkingDetails.find_one(ParkingDetails.uid == uid)
        
        if parking:
            total = getattr(parking, 'slots', 0)
            booked = getattr(parking, 'booked_slots', 0)
            acquired = getattr(parking, 'acquired_slots', 0)
            income = getattr(parking, 'total_income', 0.0)

            response = DashboardResponse(
                total_income=income,
                booked_slots=booked,
                total_slots=total,
                acquired_slots=acquired,
                available_slots=total - booked - acquired
            )
            await websocket.send_json(response.model_dump())

        while True:
            await websocket.receive_text()

    except Exception as e:
        print(f"WebSocket Error: {e}")
    finally:
        if uid:
            manager.disconnect(websocket, uid)
        
# Block end - websocket endpoints.