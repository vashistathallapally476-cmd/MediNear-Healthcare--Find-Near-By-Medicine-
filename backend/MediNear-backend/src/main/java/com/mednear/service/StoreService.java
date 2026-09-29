package com.mednear.service;

import com.mednear.dto.request.StoreRequest;
import com.mednear.dto.response.StoreResponse;
import com.mednear.entity.Role;
import com.mednear.entity.Store;
import com.mednear.entity.User;
import com.mednear.exception.BusinessException;
import com.mednear.exception.ResourceNotFoundException;
import com.mednear.exception.UnauthorizedException;
import com.mednear.repository.StoreRepository;
import com.mednear.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    @Autowired private StoreRepository storeRepository;
    @Autowired private UserRepository  userRepository;

    @Transactional
    public StoreResponse registerStore(StoreRequest req, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        if (owner.getRole() != Role.OWNER) {
            throw new UnauthorizedException("Only users with role OWNER can register stores");
        }

        Store store = new Store(req.getStoreName(), owner,
                                req.getAddress(), req.getLatitude(),
                                req.getLongitude(), req.getPhone());
        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getMyStores(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));
        return storeRepository.findByOwnerAndActiveTrue(owner)
            .stream().map(StoreResponse::from).toList();
    }

    @Transactional
    public void deactivateStore(Long storeId, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You do not own this store");
        }
        store.setActive(false);
        storeRepository.save(store);
    }
}
