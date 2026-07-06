package org.example.thuvienso.Config;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.CollectionRequest;
import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Enum.TypeCollection;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.FolderEntity;
import org.example.thuvienso.Module.RoleEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.RoleRepo;
import org.example.thuvienso.Service.CollectionService;
import org.example.thuvienso.Service.FolderService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(AccountRepo accountRepo,
                                        RoleRepo roleRepo, FolderService folderService, CollectionService collectionService) {
        return args -> {
            // Initial data setup
            if (accountRepo.findByUserName("admin").isEmpty()) {

                RoleEntity vaiTro =
                        roleRepo.findByRoleName("admin")
                                .orElseGet(() -> {

                                    RoleEntity role = RoleEntity.builder()
                                            .roleName("Admin")
                                            .isDeleted(false)
                                            .build();

                                    return roleRepo.save(role);
                                });
                RoleEntity Viewer =
                        roleRepo.findByRoleName("viewer")
                                .orElseGet(() -> {

                                    RoleEntity role = RoleEntity.builder()
                                            .roleName("viewer")
                                            .isDeleted(false)
                                            .build();

                                    return roleRepo.save(role);
                                });

                FolderResponse folderMusic=folderService.create(FolderRequest.builder()
                                .folderName("Âm Nhạc")
                                .description("Thư mục chứ các thư mục con hoặc tập tin về âm nhạc")
                                .parentFolder(null)
                        .build());
                FolderResponse folderDocument=folderService.create(FolderRequest.builder()
                        .folderName("Tài liệu")
                        .description("Thư mục chứ các thư mục con hoặc tập tin về tài liệu")
                        .parentFolder(null)
                        .build());
                FolderResponse folderImage=folderService.create(FolderRequest.builder()
                        .folderName("Hình ảnh")
                        .description("Thư mục chứ các thư mục con hoặc tập tin về hình ảnh")
                        .parentFolder(null)
                        .build());

                CollectionResponse noiBat=collectionService.create(CollectionRequest.builder()
                                .collectionName("Nổi Bật")
                                .typeCollection(TypeCollection.FEATURED.name())
                                .build());
                CollectionResponse moiNhat=collectionService.create(CollectionRequest.builder()
                        .collectionName("Mới nhất")
                        .typeCollection(TypeCollection.TODAY.name())
                        .build());
                CollectionResponse xemNhieu=collectionService.create(CollectionRequest.builder()
                        .collectionName("Xem Nhiều")
                        .typeCollection(TypeCollection.VIDEO_HOT.name())
                        .build());
                CollectionResponse homNay=collectionService.create(CollectionRequest.builder()
                        .collectionName("Hôm Nay")
                        .typeCollection(TypeCollection.TODAY.name())
                        .build());
                CollectionResponse tuanNay=collectionService.create(CollectionRequest.builder()
                        .collectionName("Tuần Này")
                        .typeCollection(TypeCollection.THIS_WEEK.name())
                        .build());
                CollectionResponse thangNay=collectionService.create(CollectionRequest.builder()
                        .collectionName("Tháng Này")
                        .typeCollection(TypeCollection.THIS_MONTH.name())
                        .build());

                AccountEntity user = AccountEntity.builder()
                        .userName("admin")
                        .accountName("admin")
                        .password(passwordEncoder.encode("admin"))
                        .createdAt(LocalDateTime.now())
                        .roleEntity(vaiTro)
                        .isDeleted(false)
                        .build();

                accountRepo.save(user);
            }

            log.warn("user admin created with default password username is admin");
        };
    }
}
