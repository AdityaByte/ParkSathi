""" "this file has all the admin routes."""
from beanie.odm.queries.update import UpdateQuery
from beanie.operators import Set
from fastapi import HTTPException
from app.model.parking import VerificationStatus, ParkingDetails
import logging

async def get_pending_owner_verification_form()-> list[ParkingDetails]:
    """This function finds the forms whose the verification status are pending from the mongodb and return them"""
    return await ParkingDetails.find_many(ParkingDetails.verification_status == VerificationStatus.PENDING).to_list() 
    
async def change_owner_form_verification_status(uid: str, status: VerificationStatus) -> ParkingDetails:
    """Find the parking form and update the verification status."""
    doc = await ParkingDetails.find_one(ParkingDetails.uid == uid)
    if not doc:
        raise HTTPException(status_code=404, detail="Form not found")
    doc.verification_status = status
    await doc.save()
    logging.info(f"Changed verification status of form {uid} to {status}")
    return doc