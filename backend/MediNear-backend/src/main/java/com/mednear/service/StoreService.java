package com.mednear.service;

import com.mednear.dto.request.StoreRequest;
import com.mednear.dto.response.StoreResponse;
import com.mednear.entity.Role;
import com.mednear.entity.Store;
import com.mednear.entity.User;
import com.mednear.exception.ResourceNotFoundException;
import com.mednear.exception.UnauthorizedException;
import com.mednear.repository.StoreRepository;
import com.mednear.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    /**
     * Register a new pharmacy.
     */
    @Transactional
    public StoreResponse registerStore(
            StoreRequest req,
            String ownerEmail
    ) {

        User owner =
            userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found: " + ownerEmail
                    )
                );

        if (owner.getRole() != Role.OWNER) {
            throw new UnauthorizedException(
                "Only users with role OWNER can register stores"
            );
        }

        Store store = new Store(
            req.getStoreName(),
            owner,
            req.getAddress(),
            req.getLatitude(),
            req.getLongitude(),
            req.getPhone()
        );

        Store savedStore =
            storeRepository.save(store);

        return StoreResponse.from(savedStore);
    }

    /**
     * Get all active stores owned by the current owner.
     */
    @Transactional(readOnly = true)
    public List<StoreResponse> getMyStores(
            String ownerEmail
    ) {

        User owner =
            userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User not found: " + ownerEmail
                    )
                );

        return storeRepository
            .findByOwnerAndActiveTrue(owner)
            .stream()
            .map(StoreResponse::from)
            .toList();
    }

    /**
     * Upload or replace the pharmacy image.
     */
    @Transactional
    public StoreResponse uploadPharmacyImage(
            Long storeId,
            MultipartFile image,
            String ownerEmail
    ) throws IOException {

        Store store =
            storeRepository.findById(storeId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Store",
                        storeId
                    )
                );

        /*
         * Make sure one owner cannot upload an image
         * to another owner's pharmacy.
         */
        if (!store.getOwner()
                 .getEmail()
                 .equals(ownerEmail)) {

            throw new UnauthorizedException(
                "You do not own this store"
            );
        }

        String imageUrl =
            supabaseStorageService.uploadPharmacyImage(
                image,
                storeId
            );

        store.setImageUrl(imageUrl);

        Store savedStore =
            storeRepository.save(store);

        return StoreResponse.from(savedStore);
    }

    /**
     * Soft-delete a pharmacy.
     */
    @Transactional
    public void deactivateStore(
            Long storeId,
            String ownerEmail
    ) {

        Store store =
            storeRepository.findById(storeId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Store",
                        storeId
                    )
                );

        if (!store.getOwner()
                 .getEmail()
                 .equals(ownerEmail)) {

            throw new UnauthorizedException(
                "You do not own this store"
            );
        }

        store.setActive(false);

        storeRepository.save(store);
    }
}