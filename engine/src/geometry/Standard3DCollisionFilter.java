package geometry;

import org.joml.Vector3f;

/**
 *
 * @author Andrew_2
 */
public class Standard3DCollisionFilter implements CollisionFilter {

    @Override
    public CollisionData3D collide(Collider primary, Collider secondary) {
        if (primary instanceof AABB3D && secondary instanceof AABB3D) {
            return collideAABB3D_AABB3D((AABB3D) primary, (AABB3D) secondary);
        }
        //StandardCollision2D cannot handle these colliders
        return null;
    }

    public CollisionData3D collideAABB3D_AABB3D(AABB3D primary, AABB3D secondary) {
        Vector3f priCenter = primary.getPosition(new Vector3f());
        Vector3f priHalf = primary.getHalfDimension(new Vector3f());
        Vector3f secCenter = secondary.getPosition(new Vector3f());
        Vector3f secHalf = secondary.getHalfDimension(new Vector3f());

        float cnx = -priCenter.x + secCenter.x;
        float cpx = priCenter.x + secCenter.x;

        if (priCenter.x < secCenter.x) {
            cnx -= priHalf.x + secHalf.x ;
            if (cnx > 0) {
                return null;
            }
            cpx += priHalf.x - secHalf.x;
        } else {
            cnx += priHalf.x + secHalf.x;
            if (cnx < 0) {
                return null;
            }
            cpx += -priHalf.x + secHalf.x;
        }

        float cny = -priCenter.y + secCenter.y;
        float cpy = priCenter.y + secCenter.y;

        if (priCenter.y < secCenter.y) {
            cny -= priHalf.y + secHalf.y ;
            if (cny > 0) {
                return null;
            }
            cpy += priHalf.y - secHalf.y;
        } else {
            cny += priHalf.y + secHalf.y;
            if (cny < 0) {
                return null;
            }
            cpy += -priHalf.y + secHalf.y;
        }

        float cnz = -priCenter.z + secCenter.z;
        float cpz = priCenter.z + secCenter.z;

        if (priCenter.z < secCenter.z) {
            cnz -= priHalf.z + secHalf.z ;
            if (cnz > 0) {
                return null;
            }
            cpz += priHalf.z - secHalf.z;
        } else {
            cnz += priHalf.z + secHalf.z;
            if (cnz < 0) {
                return null;
            }
            cpz += -priHalf.z + secHalf.z;
        }

        Vector3f min = priCenter.set(Math.abs(cnx), Math.abs(cny), Math.abs(cnz));
        if (min.x < min.y) {
            cny = 0;
            if (min.x < min.z) {
                cnz = 0;
            } else {
                cnx = 0;
            }
        } else {
            cnx = 0;
            if (min.y < min.z) {
                cnz = 0;
            } else {
                cny = 0;
            }
        }

        return new CollisionData3D(primary, secondary, new Vector3f(cpx, cpy, cpz).mul(0.5f), new Vector3f(cnx, cny, cnz).mul(0.5f));
    }

}
