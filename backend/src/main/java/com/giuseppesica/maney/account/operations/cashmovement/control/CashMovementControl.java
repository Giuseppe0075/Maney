package com.giuseppesica.maney.account.operations.cashmovement.control;

import com.giuseppesica.maney.account.liquidityaccount.model.LiquidityAccount;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovementDto;
import com.giuseppesica.maney.account.operations.cashmovement.service.CashMovementService;
import com.giuseppesica.maney.account.liquidityaccount.service.LiquidityAccountService;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing cash movements (income and outcome operations).
 *
 * <p>Provides HTTP endpoints for recording financial transactions that affect a single
 * liquidity account - either adding funds (income) or removing funds (outcome). Each
 * operation automatically updates the associated account balance.</p>
 *
 * <p><strong>Base Path:</strong> {@code /user/portfolio/liquidity-accounts/cash-movements}</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Automatic balance updates using {@code @Transactional}</li>
 *   <li>Reversal of previous balance effects on update/delete</li>
 *   <li>Optional categorization for budgeting and reporting</li>
 *   <li>Portfolio-scoped access control</li>
 * </ul>
 *
 * <p><strong>Balance Update Logic:</strong></p>
 * <ul>
 *   <li>Create: Applies the movement effect to account balance</li>
 *   <li>Update: Reverts old effect, applies new effect</li>
 *   <li>Delete: Reverts the movement effect from balance</li>
 * </ul>
 *
 * @see CashMovement
 * @see CashMovementDto
 * @see CashMovementService
 * @see LiquidityAccountService
 */
@RestController
@RequestMapping("/user/portfolio/liquidity-accounts/cash-movements")
public class CashMovementControl {

    private final CashMovementService cashMovementService;
    private final LiquidityAccountService liquidityAccountService;
    private final CategoryService categoryService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param cashMovementService service for cash movement persistence
     * @param liquidityAccountService service for account balance updates
     * @param categoryService service for category resolution and validation
     * @param authenticationHelper helper for user authentication and authorization
     */
    @Autowired
    public CashMovementControl(CashMovementService cashMovementService, LiquidityAccountService liquidityAccountService, CategoryService categoryService, AuthenticationHelper authenticationHelper) {
        this.cashMovementService = cashMovementService;
        this.liquidityAccountService = liquidityAccountService;
        this.categoryService = categoryService;
        this.authenticationHelper = authenticationHelper;
    }

    /**
     * Retrieves all cash movements for the authenticated user.
     *
     * <p>Returns movements from all liquidity accounts in the user's portfolio,
     * useful for building transaction history and financial reports.</p>
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with HTTP 200 and list of movement DTOs
     */
    @GetMapping
    public ResponseEntity<List<CashMovementDto>> getCashMovements(
            Authentication authentication
    ){
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        List<CashMovement> cashMovements = cashMovementService.getCashMovementsByUserId(user);
        List<CashMovementDto> cashMovementDtos =
                cashMovements.stream()
                        .map(CashMovementDto::new)
                        .toList();
        return ResponseEntity.ok(cashMovementDtos);
    }

    /**
     * Retrieves a specific cash movement by ID.
     *
     * <p>Validates that the movement belongs to the user's portfolio before returning data.</p>
     *
     * @param authentication Spring Security authentication object
     * @param id the cash movement ID to retrieve
     * @return ResponseEntity with HTTP 200 and the movement DTO
     * @throws NotFoundException if movement doesn't exist or doesn't belong to user
     */
    @GetMapping("/{id}")
    public ResponseEntity<CashMovementDto> getCashMovementById(
            Authentication authentication,
            @PathVariable Long id
            ){
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        CashMovement cashMovement = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        return ResponseEntity.ok(new CashMovementDto(cashMovement));
    }

    /**
     * Creates a new cash movement and updates the account balance.
     *
     * <p>This endpoint performs the following operations atomically:</p>
     * <ol>
     *   <li>Validates the account exists and belongs to user's portfolio</li>
     *   <li>Validates the category exists and belongs to the user (if provided)</li>
     *   <li>Creates the cash movement record</li>
     *   <li>Updates the account balance based on movement type:
     *     <ul>
     *       <li>INCOME: balance += amount</li>
     *       <li>OUTCOME: balance -= amount</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>The entire operation is transactional - if any step fails, no changes are committed.</p>
     *
     * @param authentication Spring Security authentication object
     * @param cashMovementDto DTO containing movement details (account, amount, type, date, category, note)
     * @return ResponseEntity with HTTP 200 and the created movement DTO
     * @throws NotFoundException if account or category is not found
     */
    @PostMapping
    @Transactional
    public ResponseEntity<CashMovementDto> createCashMovement(
            Authentication authentication,
            @Valid @RequestBody CashMovementDto cashMovementDto
    ){
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        
        LiquidityAccount liquidityAccount = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(
                        user.getPortfolio().getId(),
                        cashMovementDto.getLiquidityAccountName()
                )
                .orElseThrow(() -> new NotFoundException("Liquidity Account Not Found"));

        Category category = categoryService.findByUserAndId(
                user.getId(),
                cashMovementDto.getCategoryId()
        ).orElseThrow(() -> new NotFoundException("Category Not Found"));

        CashMovement cashMovement = new CashMovement();
        cashMovement.setDate(cashMovementDto.getDate());
        cashMovement.setNote(cashMovementDto.getNote());
        cashMovement.setCategory(category);
        cashMovement.setAmount(cashMovementDto.getAmount());
        cashMovement.setType(cashMovementDto.getType());
        cashMovement.setLiquidityAccount(liquidityAccount);

        cashMovement = cashMovementService.saveCashMovement(cashMovement);

        liquidityAccountService.updateLiquidityAccount(
                liquidityAccount,
                cashMovementDto.getAmount(),
                cashMovementDto.getType()
        );

        return ResponseEntity.ok(new CashMovementDto(cashMovement));
    }

    /**
     * Updates an existing cash movement with new details.
     *
     * <p>This endpoint handles movement modifications by performing a two-phase balance update:</p>
     * <ol>
     *   <li><strong>Reversal Phase:</strong> Reverts the original balance effect by applying
     *       the inverse operation (INCOME→OUTCOME or OUTCOME→INCOME) with the old amount</li>
     *   <li><strong>Application Phase:</strong> Applies the new movement details with potentially
     *       different amount or type</li>
     * </ol>
     *
     * <p>This approach ensures balance integrity even when amount or type changes.
     * The account affected remains the same (account switching not supported in updates).</p>
     *
     * <p><strong>Note:</strong> Category updates are not currently supported.
     * The movement retains its original category assignment.</p>
     *
     * @param authentication Spring Security authentication object
     * @param id the cash movement ID to update
     * @param cashMovementDto DTO containing updated movement details
     * @return ResponseEntity with HTTP 200 and the updated movement DTO
     * @throws NotFoundException if the movement doesn't exist or doesn't belong to user
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<CashMovementDto> updateCashMovement(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CashMovementDto cashMovementDto
    ){
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        CashMovement cashMovementToUpdate = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        // Revert previous cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToUpdate.getLiquidityAccount(),
                cashMovementToUpdate.getAmount(),
                cashMovementToUpdate.getType() == CashMovementType.INCOME ? CashMovementType.OUTCOME : CashMovementType.INCOME
        );
        // Apply new cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToUpdate.getLiquidityAccount(),
                cashMovementDto.getAmount(),
                cashMovementDto.getType()
        );

        // Save cash movement changes
        cashMovementToUpdate.setDate(cashMovementDto.getDate());
        cashMovementToUpdate.setNote(cashMovementDto.getNote());
        cashMovementToUpdate.setAmount(cashMovementDto.getAmount());
        cashMovementToUpdate.setType(cashMovementDto.getType());
        CashMovement updatedCm = cashMovementService.saveCashMovement(cashMovementToUpdate);
        return ResponseEntity.ok(new CashMovementDto(updatedCm));
    }

    /**
     * Deletes a cash movement and reverts its balance effect.
     *
     * <p>Removes the movement record from the database while automatically reversing
     * its impact on the account balance by applying the inverse operation:</p>
     * <ul>
     *   <li>If movement was INCOME: balance -= amount</li>
     *   <li>If movement was OUTCOME: balance += amount</li>
     * </ul>
     *
     * <p>The deletion and balance reversal occur within a single transaction to ensure
     * data consistency. If any operation fails, no changes are committed.</p>
     *
     * @param authentication Spring Security authentication object
     * @param id the cash movement ID to delete
     * @return ResponseEntity with HTTP 204 (No Content) on successful deletion
     * @throws NotFoundException if the movement doesn't exist or doesn't belong to user
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCashMovement(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        CashMovement cashMovementToDelete = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        // Revert cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToDelete.getLiquidityAccount(),
                cashMovementToDelete.getAmount(),
                cashMovementToDelete.getType() == CashMovementType.INCOME ? CashMovementType.OUTCOME : CashMovementType.INCOME
        );
        cashMovementService.deleteCashMovement(cashMovementToDelete);
        return ResponseEntity.noContent().build();
    }

}
