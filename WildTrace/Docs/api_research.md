# iNaturalist API Reference for WildTrace

This document summarizes the essential features of the iNaturalist REST API, focusing on the endpoints and authentication required to implement the WildTrace application's core functionality: creating user sightings (observations) and querying them for the map and search features.

The base URL for authentication and authenticated requests is:

https://www.inaturalist.org


---

## 1. Authentication for Mobile Apps

Since WildTrace is a mobile application, the **Proof Key for Code Exchange (PKCE)** flow is the recommended authentication method, as it securely handles situations where a client secret cannot be stored.

| Flow | Use Case |
|------|-----------|
| **Proof Key for Code Exchange (PKCE)** | Recommended for mobile applications; uses a hashed, single-use "code verifier" for identification. |
| **Resource Owner Password Credentials** | Alternative for desktop/mobile apps, retrieving an access token using the user's username and password. |

**Note:**  
All methods require you to first create an iNaturalist application to obtain the necessary credentials.  
Endpoints marked *"Auth required"* need a valid access token.

---

## 2. Key Endpoints for WildTrace

### A. Creating and Updating Observations (Sighting Upload)

| Endpoint | Requirement | Purpose | Essential Parameters |
|-----------|--------------|----------|------------------------|
| `POST /observations` | Auth required | Primary endpoint for creating a new observation (sighting). | `observation[species_guess]`, `observation[taxon_id]`, `observation[latitude]`, `observation[longitude]`, `local_photos[]` (for uploaded photo data) |
| `POST /observation_photos` | Auth required | Endpoint for adding iNaturalist-hosted photo data to an observation (requires a multipart request). | `observation_photo[observation_id]`, `file` (photo data) |
| `PUT /observations/:id` | Auth required | Allows updating an existing observation. | Same `observation[]` parameters as `POST`, plus observation ID |

---

### B. Retrieving and Filtering Observations (Map and Search)

| Endpoint | Requirement | Purpose | Key Parameters for Filtering |
|-----------|--------------|----------|-------------------------------|
| `GET /observations` | None (generally) | Primary endpoint for retrieving observations for map and search views. | **Geographic Filters:** `swlat`, `swlng`, `nelat`, `nelng` (bounding box) <br> **Species Filters:** `taxon_id`, `taxon_name` <br> **Quality Filters:** `quality_grade` (e.g., casual, research) <br> **Data Filters:** `has[]=photos`, `has[]=geo` |
| `GET /observations/:id` | None | Retrieves detailed information about a single observation for the `SightingDetailView`. | Observation ID |

---

### C. Creating Identifications

| Endpoint | Requirement | Purpose | Key Parameters |
|-----------|--------------|----------|----------------|
| `POST /identifications` | Auth required | Creates an identification to confirm or suggest a species for an observation. | `identification[observation_id]`, `identification[taxon_id]` |

---

## 3. References

- iNaturalist Developer Documentation: [https://api.inaturalist.org/v1/docs/](https://api.inaturalist.org/v1/docs/)
- iNaturalist Authentication Guide: [https://www.inaturalist.org/pages/api+reference](https://www.inaturalist.org/pages/api+reference)

