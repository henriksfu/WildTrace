# API Research and Selection for WildTrace

This document summarizes the initial research and decisions regarding the third-party APIs needed to enhance the core "sighting" feature. This work supports the architecture and data flow for the `SightingDetailView`.

---

## 1. AI Image Recognition APIs (Primary Goal: Identify Species)

The objective is to accurately analyze the image uploaded by the user and return a confident identification of the observed species.

### Rationale for Selection (Talking Point)

We are selecting the **iNaturalist API** because its core functionality is specifically tailored for biodiversity data. By utilizing an API built on a massive dataset of real-world species sightings, we ensure that our image identification is more relevant and robust for our application's mission than a general-purpose AI service. This choice reflects our commitment to the conservation and natural history domain.

---

## 2. Information/Content API (Primary Goal: Provide Context)

The objective here is to retrieve a brief, relevant summary about the identified species to display in the `SightingDetailView`.

### Rationale for Selection (Talking Point)

The **Wikipedia API** offers the best balance of cost-effectiveness and data quality. We will use the **'extracts'** module to fetch a concise, plain-text summary of the species' Wikipedia page, which integrates smoothly into the `SightingDetailView`.

---

## Conclusion and Show & Tell 2 Focus

Our architectural plan involves creating a dedicated **`ApiService`** layer to manage all API calls (iNaturalist and Wikipedia). This structure separates our business logic from the presentation and ensures our core data flow—from the image in Firebase Storage to the species data on the detail screen—is clean and scalable for Show & Tell 2.