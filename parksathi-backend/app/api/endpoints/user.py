"""This mainly handles the user routes"""

from beanie.operators import Set
from app.model.user import User, UserType

async def create_user(token: dict):
    try:
        uid = token['uid']
        name = token['name']
        email = token['email']
        # Finding the user and upserting it with the latest details.
        await User.find_one(User.uid == uid).upsert(
            Set({User.name: name, User.email: email}),
            on_insert=User(uid=uid, name=name, email=email, roles=[UserType.USER])
        )

        return {"success": True, "message": "User created successfully"}

    except Exception as e:
        print(f"Upsert error: {e}")
        return {"success": False, "message": f"Failed to create user, {e}"}