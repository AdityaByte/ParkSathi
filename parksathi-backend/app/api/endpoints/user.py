"""This mainly handles the user routes"""

from fastapi import HTTPException, status, Response
from http import HTTPStatus
from beanie.operators import Set
import logging

from app.model.parking import ParkingDetails
from app.model.user import User, UserType

async def create_user(response: Response, token: dict):
    try:
        uid = token['uid']
        name = token['name']
        email = token['email']
        # Finding the user and upserting it with the latest details.
        await User.find_one(User.uid == uid).upsert(
            Set({User.name: name, User.email: email}),
            on_insert=User(uid=uid, name=name, email=email, roles=[UserType.USER])
        )

        response.status_code = HTTPStatus.CREATED
        return {"message": "User created successfully"}

    except Exception as e:
        logging.error(f"Upsert error: {e}")
        response.status_code = HTTPStatus.INTERNAL_SERVER_ERROR
        return {"message": f"Failed to create user, Internal server error."}

async def verify_user_role(token: dict) -> dict:
    uid = token['uid']
    if not uid:
        logging.error("Invalid token: UID missing.")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token: uid missing"
        )

    user = await User.find_one(User.uid == uid)
    if user is None:
        logging.error("No user found of the uid.")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No user found with this UID"
        )

    parking_details = await ParkingDetails.find_one(ParkingDetails.uid == user.uid)

    if parking_details is None:
        logging.error("No parking details were found associated with the user.")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No parking details found for this user"
        )

    is_owner: bool = UserType.OWNER in user.roles

    # Return a clean dictionary
    return {
        "uid": user.uid,
        "is_owner": is_owner,
        "verification_status": parking_details.verification_status
    }